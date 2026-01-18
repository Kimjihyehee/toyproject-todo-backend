package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.UserDto;
import com.example.backend_toyproject.model.dto.user.UserCreateDto;
import com.example.backend_toyproject.model.entity.UserEntity;
import com.example.backend_toyproject.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /*
     * 1. 유저 생성
     */
    public UserDto createUser(UserCreateDto userCreateDto) {
        // nickname 중복 사용자가 있는지 확인후 있으면 예외처리
        if (userRepository.existsByNickname(userCreateDto.getNickname())) {
            throw new IllegalArgumentException("nickname is duplicated");
        }
        // Dto -> Entity 변환
        UserEntity userEntity = new UserEntity(userCreateDto);
        // 저장 -> Dto 변환
        return new UserDto(userRepository.save(userEntity));
    }

    /*
     * 2. 유저 정보 조회(단일 유저)
     */
    public UserDto getUserTodo(String nickName) {
        // 1. user목록에 nickName(pathParam)이 존재하는지 체크
        if(!userRepository.existsByNickname(nickName)) {
            // 존재하지 않는 경우
            throw new IllegalArgumentException("user is not exist");
        }
        // 2. 유저(삭제되지 않고, 해당 nickName을 가진 유저)정보 조회
        UserEntity userEntity = userRepository.findByNicknameAndDeletedAtIsNull(nickName)
                .orElseThrow(() -> new IllegalArgumentException("user is not exist or deleted"));
        // 3. 유저정보 entity 형태 -> DTO 변형
        return new UserDto(userEntity);
    }
}
