package com.example.backend_toyproject.model.dto.user;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDto {
    @NotBlank
    private String name;
    @NotBlank
    @Size(max = 50)
    private String nickname;
}
