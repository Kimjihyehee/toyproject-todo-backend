package com.example.backend_toyproject.model.entity;

import com.example.backend_toyproject.model.dto.UserDto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "todo_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nickname", nullable = false, length = 50, unique = true)
    private String nickname;

    @CreationTimestamp
    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "modified_timestamp", updatable = false)
    private Timestamp modifiedAt;

    @UpdateTimestamp
    @Column(name = "lastLogin_timestamp", updatable = false)
    private Timestamp lastLoginAt;

    @Column(name = "deleted_timestamp")
    private Timestamp deletedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TodoEntity> todos = new ArrayList<>();

    // DTO -> Entity 변환
//    public UserEntity(UserDto userDto) {
//        this.name = userDto.getName() != null ? userDto.getName() : "";
//        this.nickname = userDto.getNickname() != null ? userDto.getNickname() : "";
//    }

    // 유저 생성 시 필요한 생성
    public UserEntity(UserDto dto) {
        this.name = dto.getName() != null ? dto.getName() : "";
        this.nickname = dto.getNickname() != null ? dto.getNickname() : "";
    }

}
