package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.TodoDto;
import com.example.backend_toyproject.model.dto.todoUpdate.TodoUpdateRequestDTO;
import com.example.backend_toyproject.model.entity.*;
import com.example.backend_toyproject.model.enums.SortDirection;
import com.example.backend_toyproject.model.enums.SortType;
import com.example.backend_toyproject.model.enums.TodoStatus;
import com.example.backend_toyproject.model.enums.TodoViewType;
import com.example.backend_toyproject.repository.CategoryRepository;
import com.example.backend_toyproject.repository.TodoRepository;
import com.example.backend_toyproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
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
        return new TodoDto(todoRepository.save(todoEntity));
    }

    /*
     * 2. 할일 전체 조회(단일 유저)
     * 필터 유형 : year, Month, day 
     * 정렬 유형 : 생성일순, 마감일순, 우선순위순, 완료/미완료순  - 1개만 선택 가능 & 기본값 : 생성일순
     * 정렬 방향 : ACS(오름차순), DESC(내림차순)        - null 가능 & 각 필터유형마다 정렬 기본값이 상이
     */
    @Transactional(readOnly = true)
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
        // 0. sortType, direction, year null 체크 및 page/size 유효성 체크
        // year 필수 값으로 
        if (year == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "year는 필수입니다.");
        }
        // sortType null이면, CREATED_AT로 설정
        if (sortType == null) {
            sortType = SortType.CREATED_AT;
        }
        // direction null이면, DESC로 설정
        if (direction == null) {
            direction = SortDirection.DESC;
        }

        // page/size 유효성 체크
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page는 0 이상이어야 합니다.");
        }
        if (size <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size는 1 이상이어야 합니다.");
        }

        // 1. 사용자 존재 확인
        userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 2. param : viewType은 Month 이면서,
        // month가 값이 없고, day의 값이 존재 -> 예외 처리
        if (viewType == TodoViewType.MONTH && month == null && day != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "월이 없는 일 조회는 불가합니다.");
        }

         // 3. month, day 존재할 수 없는 값 체크
        if (month != null && (month < 1 || month > 12)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "month는 1~12 사이여야 합니다.");
        }
        if (day != null && (day < 1 || day > 31)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "day는 1~31 사이여야 합니다.");
        }

        // 4. day만 있고 month가 없으면 400 처리
        if (day != null && month == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "day는 month와 함께 전달되어야 합니다.");
        }

        // 5. month+day 조합 유효성 검증 (예: 2/30, 4/31 차단)
        if (month != null && day != null) {
            try {
                LocalDate.of(year, month, day);
            } catch (DateTimeException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "유효하지 않은 날짜입니다: %d-%02d-%02d".formatted(year, month, day)
                );
            }
        }

        // 6. 기간(startDate~endDate)이 조회하려는 기간(year/month/day)과 겹치면 조회
        // Timestamp 타입 변수 선언
        Timestamp startTs;
        Timestamp endTs;

        if(month != null && day == null) { // 입력 : 년월
            try {
                // MONTH 조회
                LocalDateTime queryStart = LocalDateTime.of(year, month, 1, 0, 0);
                LocalDateTime queryEnd = queryStart.plusMonths(1);

                startTs = Timestamp.valueOf(queryStart);
                endTs = Timestamp.valueOf(queryEnd);
            }
            catch (DateTimeException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "유효하지 않은 날짜입니다: %d-%02d".formatted(year, month)
                );
            }
        }
        else if (month != null) { // 입력 : 년월일
            try {
                LocalDateTime queryStart = LocalDateTime.of(year, month, day, 0, 0);
                LocalDateTime queryEnd = queryStart.plusDays(1);

                startTs = Timestamp.valueOf(queryStart);
                endTs = Timestamp.valueOf(queryEnd);
            } catch (DateTimeException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "유효하지 않은 날짜입니다: %d-%02d-%02d".formatted(year, month, day)
                );
            }
        } else {
            // YEAR 조회 (선택)
            LocalDateTime queryStart = LocalDateTime.of(year, 1, 1, 0, 0);
            LocalDateTime queryEnd = queryStart.plusYears(1);

            startTs = Timestamp.valueOf(queryStart);
            endTs = Timestamp.valueOf(queryEnd);
        }

        // 7. 정렬 필드 결정: sortType(enum) -> Entity 필드명(String) 매핑
        String sortField = resolveSortField(sortType);
        // 8. 정렬 방향 결정: direction(도메인 enum) -> Spring Data Sort.Direction(ASC/DESC) 변환
        Sort.Direction sortDirection = resolveDirection(direction);
        // 9. Sort + Pageable 생성
        Sort sort = Sort.by(sortDirection, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 10. 조회: 삭제되지 않은(deletedAt IS NULL) 할 일 중, [todo.startDate < queryEnd] AND [todo.endDate > queryStart] 로 기간이 겹치는 데이터 페이징 조회
        Page<TodoEntity> todoPage =
                todoRepository.findByUser_IdAndDeletedAtIsNullAndStartDateLessThanAndEndDateGreaterThan(
                        userId,
                        endTs,     // Todo.startDate < queryEnd
                        startTs,   // Todo.endDate   > queryStart
                        pageable
                );
        // 11. Entity -> DTO 변환된 값으로 return
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
    public TodoDto updateTodo(TodoUpdateRequestDTO dto) {
        // 0. 유저 존재 확인
        userRepository.findById(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found: " + dto.getUserId()));

        // 1. 유저 해당 할일 항목의 정보(삭제되지 않은 할일만)를 조회
        TodoEntity todo = todoRepository.findByIdAndUser_IdAndDeletedAtIsNull( dto.getTodoId(), dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("Todo not found:"));

        // ------------------------------- StartDate, EndDate 설정 -------------------------------
        // StartDate가 null -> 기존값 유지
        LocalDateTime start = dto.getStartDate() != null
                ? dto.getStartDate().toLocalDateTime()
                : todo.getStartDate().toLocalDateTime();

        // EndDate가 null -> 기존값 유지
        LocalDateTime end = dto.getEndDate() != null
                ? dto.getEndDate().toLocalDateTime()
                : todo.getEndDate().toLocalDateTime();

        // 검증
        if (!start.isBefore(end)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startDate는 endDate보다 이전이어야 합니다."
            );
        }

         // 검증 통과 시, 반영
        todo.setStartDate(Timestamp.valueOf(start));
        todo.setEndDate(Timestamp.valueOf(end));
        // --------------------------------------------------------------------------------------

        // 수정 여부 변수선언
        boolean isUpdated = false;
        // 완료 상태 변경 여부 플래그
        boolean isCompletedUpdated = false;

        // 수정값 update
        if(dto.getTitle() != null) {
            todo.setTitle(dto.getTitle());
            isUpdated = true;
        }
        if(dto.getDescription() != null) {
            todo.setDescription(dto.getDescription());
            isUpdated = true;
        }
        if(dto.getPriority() != null) {
            todo.setPriority(dto.getPriority());    
            isUpdated = true;
        }
        if(dto.getCompleted() != null) {
            todo.setCompleted(dto.getCompleted());
            isUpdated = true;
            isCompletedUpdated = true;
        }
        // category 필드 수정 의도 있을 경우
        if(dto.getCategories() != null) {
            // 1. 기존 매핑(categoryLinks) 제거
            todo.getCategoryLinks().clear();

            // 2. 요청된 카테고리 이름 목록에서 중복 여부를 먼저 검증
            //    중복이면 입력 오류로 간주하고 예외 처리
            List<String> rawNames = dto.getCategories();
            List<String> names = rawNames.stream().distinct().toList();
            if (rawNames.size() != names.size()) {
                throw new IllegalArgumentException("Duplicate category names");
            }
            // 3. DB에서 유저 소유 카테고리 중 이름이 names에 포함되는 항목 조회
            List<CategoryEntity> categories = categoryRepository.findAllByUser_IdAndNameIn(dto.getUserId(), names);
            // 4. 요청 이름과 조회 결과의 개수가 다르면 존재하지 않는 카테고리가 포함된 상황이므로 예외처리
             if (categories.size() != names.size()) {
                 throw new IllegalArgumentException("Some categories not found");
             }
            // 5. 매핑 재생성
            for (CategoryEntity category : categories) {
                // todoEntity를 선언한 CategoryLinks 재생성
                todo.getCategoryLinks()
                        .add(new TodoCategoryMappingEntity(todo, category));
            }
            isUpdated = true;
        }

        // 완료 변경이 없는 일반 수정일 때만 UPDATED로 상태 갱신
        if(isUpdated && !isCompletedUpdated) {
            todo.setStatus(TodoStatus.UPDATED);
        }

        return new TodoDto(todo);
    }

    /*
     * 4. 할일 단건 조회 (단일 유저)
     */
    @Transactional(readOnly = true)
    public TodoDto getTodoDetail(UUID userId, UUID todoId) {
        // 1. 유저 존재 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        TodoEntity todo = todoRepository.findByIdAndUser_IdAndDeletedAtIsNull(todoId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Todo not found: " + todoId)
                );

        return new TodoDto(todo);
    }

    /*
     * 5. 할일 삭제
     */
    public TodoDto deleteTodo(UUID userId, UUID todoId) {
        // 1. 유저 존재 확인
        userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        // 2. 해당하는 할일 항목 찾기
        // 없는 경우, 예외 처리
        TodoEntity todo = todoRepository.findByIdAndUser_IdAndDeletedAtIsNull(todoId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Todo not found: " + todoId)
                );
        // 3. deletedAt에 값 set
        todo.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));

        return new TodoDto(todo);
    }
}
