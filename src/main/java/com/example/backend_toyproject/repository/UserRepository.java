package com.example.backend_toyproject.repository;

import com.example.backend_toyproject.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    // 1. 유저 생성 -> 생성요청한 닉네임값을 가진 유저가 존재하는지 확인
    boolean existsByNickname(String nickname);
}


