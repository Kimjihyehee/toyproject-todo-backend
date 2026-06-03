package com.example.backend_toyproject.model.dto.user;
import lombok.*;
import java.sql.Timestamp;
import java.util.UUID;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDto {
    private UUID id;
    private String name;
    private String nickname;
    private Timestamp createdAt;
}
