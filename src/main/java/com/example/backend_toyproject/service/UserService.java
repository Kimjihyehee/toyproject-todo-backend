package com.example.backend_toyproject.service;

import com.example.backend_toyproject.model.dto.UserDto;
import com.example.backend_toyproject.model.dto.user.UserCreateDto;
import com.example.backend_toyproject.model.entity.UserEntity;
import com.example.backend_toyproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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

}
