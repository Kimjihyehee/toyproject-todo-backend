package com.example.backend_toyproject.repository;

import com.example.backend_toyproject.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    // 1. [조회] 동일한 닉네임값을 가진 유저가 존재하는지 확인
    boolean existsByNicknameAndDeletedAtIsNull(String nickname);
    // 2. [조회] 유저 정보 조회
    Optional<UserEntity> findByNicknameAndDeletedAtIsNull(String nickname);
}
