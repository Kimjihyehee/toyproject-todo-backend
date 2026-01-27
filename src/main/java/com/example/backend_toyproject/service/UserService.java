package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.UserDto;
import com.example.backend_toyproject.model.entity.UserEntity;
import com.example.backend_toyproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /*
     * 1. 유저 생성
     */
    public UserDto createUser(UserDto dto) {
        // nickname 중복 사용자가 있는지 확인후 있으면 예외처리
        if (userRepository.existsByNicknameAndDeletedAtIsNull(dto.getNickname())) {
            throw new IllegalArgumentException("nickname is duplicated");
        }
        // Dto -> Entity 변환
        UserEntity userEntity = new UserEntity(dto);
        // 저장 -> Dto 변환
        return new UserDto(userRepository.save(userEntity));
    }

    /*
     * 2. 유저 정보 조회(단일 유저)
     */
    @Transactional(readOnly = true)
    public UserDto getUserTodo(String nickName) {
        // 1. user목록에 nickName(pathParam)이 존재하는지 체크
        if(!userRepository.existsByNicknameAndDeletedAtIsNull(nickName)) {
            // 존재하지 않는 경우
            throw new IllegalArgumentException("user is not exist");
        }
        // 2. 유저(삭제되지 않고, 해당 nickName을 가진 유저)정보 조회
        UserEntity userEntity = userRepository.findByNicknameAndDeletedAtIsNull(nickName)
                .orElseThrow(() -> new IllegalArgumentException("user is not exist or deleted"));
        // 3. 유저정보 entity 형태 -> DTO 변형
        return new UserDto(userEntity);
    }

    /*
     * 3. 유저 정보 수정
     */
    public UserDto updateUser(String nickName, UserDto dto) {
        // 1. user목록에 nickName 중복값이 존재하는지 체크
        if(!userRepository.existsByNicknameAndDeletedAtIsNull(nickName)) {
            // 존재하지 않는 경우
            throw new IllegalArgumentException("user is not exist");
        }

        // 2. 삭제된 유저인지 체크
        UserEntity userEntity = userRepository.findByNicknameAndDeletedAtIsNull(nickName)
                .orElseThrow(() -> new IllegalArgumentException("nickName을 조회한 결과, 탈퇴한 회원입니다."));

        // name 수정요청한 경우
        if(dto.getName() != null) {
            // 기존 값과 동일한 값을 수정하려는 경우 예외처리
            if(userEntity.getName().equals(dto.getName())) {
                throw new IllegalArgumentException("기존의 name과 동일한 name을 입력했습니다.");
            }
            userEntity.setName(dto.getName());
        }
        // nickName 수정요청한 경우
        if(dto.getNickname()!= null) {
            // 기존 값과 동일한 값을 수정하려하는 경우 예외처리
            if(userEntity.getNickname().equals(dto.getNickname())) {
                throw new IllegalArgumentException("기존의 nickName과 동일한 nickName을 입력했습니다.");
            }

            userRepository.existsByNicknameAndDeletedAtIsNull(dto.getNickname());
//            userRepository.findByNicknameAndDeletedAtIsNull(dto.getNickname()).orElseThrow(
//                    () -> new RuntimeException("이미 사용중인 닉네임이 존재합니다."));

            userEntity.setNickname(dto.getNickname());
        }
        return new UserDto(userEntity);
    }

     /*
     * 4. 유저 삭제
     */
     public UserDto deleteUser(String nickName) {
         // 1. 존재하지 않는 nickName을 가진 사용자인지 확인
        if(!userRepository.existsByNicknameAndDeletedAtIsNull(nickName)) {
            throw new IllegalArgumentException("존재하지 않는 사용자 입니다.");
        }
        // 2. 이미 삭제된 사용자인지 확인
        UserEntity userEntity = userRepository.findByNicknameAndDeletedAtIsNull(nickName)
                .orElseThrow(() -> new IllegalArgumentException("이미 삭제된 사용자 입니다."));

        // 3. deleted_At 값 추가
         userEntity.setDeletedAt(new Timestamp(System.currentTimeMillis()));
        // 4. 저장후 DTO 변환
         return new UserDto(userEntity);
     }
}
