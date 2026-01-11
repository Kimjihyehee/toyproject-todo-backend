package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.TodoDto;
import com.example.backend_toyproject.model.dto.todoUpdate.TodoUpdateRequestDTO;
import com.example.backend_toyproject.model.entity.*;
import com.example.backend_toyproject.model.enums.SortDirection;
import com.example.backend_toyproject.model.enums.SortType;
import com.example.backend_toyproject.model.enums.TodoViewType;
import com.example.backend_toyproject.repository.CategoryRepository;
import com.example.backend_toyproject.repository.TodoRepository;
import com.example.backend_toyproject.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /*
    * 1. 할일 생성
    * */
    public TodoDto createTodo(TodoDto todoDto) {
        // userId null 체크
        UUID userId = todoDto.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId is required to create a todo");
        }
        // 사용자인지 체크
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // DTO -> Entity 변환
        TodoEntity todoEntity = new TodoEntity(todoDto);
        // user 정보 Entity에 연결
        todoEntity.setUser(user);
        // 저장
        TodoEntity savedTodoEntity = todoRepository.save(todoEntity);
        return new TodoDto(savedTodoEntity);
    }

    /*
     * 2. 할일 전체 조회(단일 유저)
     * 필터 유형 : year, Month, day 
     * 정렬 유형 : 생성일순, 마감일순, 우선순위순, 완료/미완료순  - 1개만 선택 가능 & 기본값 : 생성일순
     * 정렬 방향 : ACS(오름차순), DESC(내림차순)        - null 가능 & 각 필터유형마다 정렬 기본값이 상이
     */
    public List<TodoDto> getTodo(
            UUID userId,
            TodoViewType viewType,
            Integer year,
            Integer month,
            Integer day,
            SortType sortType,
            SortDirection direction,
            int page,
            int size
    ) {
        // 1. 사용자 존재 확인
        userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 2. param : viewType은 Month 이면서,
        // month가 값이 없고, day의 값이 존재 -> 에러처리
        if(viewType == TodoViewType.MONTH && month == null && day != null) {
            throw new IllegalArgumentException("월이 없는 일 조회는 불가합니다.");
        }

        // 3. month, day 존재할 수 없는 값 체크
        if (month != null && (month < 1 || month > 12)) {
            throw new IllegalArgumentException("month는 1~12 사이여야 합니다.");
        }

        if (day != null && (day < 1 || day > 31)) {
            throw new IllegalArgumentException("day는 1~31 사이여야 합니다.");
        }

        // 4. 기간(startDate~endDate)이 조회하려는 기간(year/month/day)과 겹치면 조회
        // Timestamp 타입 변수 선언
        Timestamp startTs;
        Timestamp endTs;

        if(month != null && day == null) { // 입력 조회기간: 년월
            // MONTH 조회
            LocalDateTime queryStart = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime queryEnd = queryStart.plusMonths(1);

            startTs = Timestamp.valueOf(queryStart);
            endTs = Timestamp.valueOf(queryEnd);
        } else if(month != null && day != null){ // 입력 조회기간: 년월일
            // DAY 조회
            LocalDateTime queryStart = LocalDateTime.of(year, month, day, 0, 0);
            LocalDateTime queryEnd = queryStart.plusDays(1);

            startTs = Timestamp.valueOf(queryStart);
            endTs = Timestamp.valueOf(queryEnd);
        } else {
            // YEAR 조회 (선택)
            LocalDateTime queryStart = LocalDateTime.of(year, 1, 1, 0, 0);
            LocalDateTime queryEnd = queryStart.plusYears(1);

            startTs = Timestamp.valueOf(queryStart);
            endTs = Timestamp.valueOf(queryEnd);
        }

        // 4. 정렬 필드 결정
        String sortField = resolveSortField(sortType);
        // 5. 정렬 방향 결정
        Sort.Direction sortDirection = resolveDirection(direction);
        // 6. Sort + Pageable 생성
        Sort sort = Sort.by(sortDirection, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 7. 조회 (기간 겹침 조건)
        Page<TodoEntity> todoPage =
                todoRepository.findByUser_IdAndStartDateLessThanAndEndDateGreaterThan(
                        userId,
                        endTs,     // Todo.startDate < queryEnd
                        startTs,   // Todo.endDate   > queryStart
                        pageable
                );
        // 8. Entity -> DTO 변환된 값으로 return
        return todoPage.getContent()
                .stream()
                .map(TodoDto::new)
                .toList();
    }

    /*
     * enum으로 표현된 “정렬 의미” -> 실제 DB(Entity) 필드명으로 변환하는 함수
     */
    private String resolveSortField(SortType sortType) {
        return switch (sortType) {
            case CREATED_AT ->  "createdAt";
            case START_DATE ->   "startDate";
            case END_DATE ->  "endDate";
            case PRIORITY ->  "priority";
            case COMPLETED ->  "completed";
        };
    }

    /*
     * enum 정렬 방향 -> Spring Direction 변환하는 함수
     */
    private Sort.Direction resolveDirection(SortDirection direction) {
        return direction == SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
    }

    /*
     * 3. 할일 수정 (단일 유저)
     * 수정 가능한 필드 : title, description, startDate, endDate, Priority, completed, categories
     */
    @Transactional
    public TodoDto updateTodo(TodoUpdateRequestDTO dto) {

        // 0. 유저 존재 확인
        userRepository.findById(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found: " + dto.getUserId()));

        // 1. 유저 해당 할일 항목의 정보를 조회
        TodoEntity todo = todoRepository.findById(dto.getTodoId()).orElseThrow(() -> new IllegalArgumentException("Todo not found:"));

        // 수정하려 한 필드를 set
        if(dto.getTitle() != null) {
            todo.setTitle(dto.getTitle());
        }
        if(dto.getDescription() != null) {
            todo.setDescription(dto.getDescription());
        }
        if(dto.getStartDate() != null) {
            todo.setStartDate(dto.getStartDate());
        }
        if(dto.getEndDate() != null) {
            todo.setEndDate(dto.getEndDate());
        }
        if(dto.getPriority() != null) {
            todo.setPriority(dto.getPriority());
        }
        if(dto.getCompleted() != null) {
            todo.setCompleted(dto.getCompleted());
        }
        // category 필드 수정 의도 있을 경우
        if(dto.getCategories() != null) {
            // 1. 기존 매핑(categoryLinks) 제거
            todo.getCategoryLinks().clear();

            // category에 있는 값을 수정요청했는지 요청값 존재 확인 (list형태로 입력되고, 그 값들중 categoryRepository의 name값에 속하는지 각각 확인해야함)
            // 2. 수정요청받는 DTO의 카테고리 이름 목록에서 중복제거 후 list로 묶음
            List<String> names = dto.getCategories().stream().distinct().toList();
            // 3. DB에서 이 유저 소유인 카테고리 중에서, 이름이 names에 포함되는 것들 모두 전부 조회
            List<CategoryEntity> categories = categoryRepository.findAllByUser_IdAndNameIn(dto.getUserId(), names);
            // 4. 검증 names(수정요청으로 들어온 카테고리 이름들(중복제거후))와, categories(DB에서 실제로 찾아온 CategoryEntity 목록)의 사이즈를 비교
            // 다르다면, 올바로 category가 매핑 처리 되지 않은 것이므로 에러처리
            if (categories.size() != dto.getCategories().size()) {
                throw new IllegalArgumentException("Some categories not found");
            }
            // 5. 매핑 재생성
            for (CategoryEntity category : categories) {
                // todoEntity를 선언한 CategoryLinks 재생성
                todo.getCategoryLinks()
                        .add(new TodoCategoryMappingEntity(todo, category));
            }
        }
        return new TodoDto(todo);
    }
}
