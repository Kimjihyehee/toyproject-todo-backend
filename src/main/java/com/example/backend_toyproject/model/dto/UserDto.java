package com.example.backend_toyproject.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class UserDto {
    @Schema(description = "유저의 id", example = "00000000-0000-4000-a000-000000000000")
    private UUID id;
    @NotNull(message = "name은 필수입니다.")
    @Schema(description = "유저의 이름", example = "유저의 이름입니다.")
    private String name;
    @NotNull(message = "nickname은 필수입니다.")
    @Schema(description = "유저의 닉네임", example = "유저의 닉네임입니다.")
    private String nickname;
    @Schema(description = "계정 생성 시간", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private Timestamp createdAt;
}
