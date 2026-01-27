package com.example.backend_toyproject.model.dto.todoUpdate;

import com.example.backend_toyproject.model.enums.Priority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoUpdateRequestDTO {
    @NotNull
    private UUID todoId;
    @NotNull
    private UUID userId; // TODO : 필요한 필드인지 확인 필요
    private String title;
    private String description;
    private Timestamp startDate;
    private Timestamp endDate;
    private Priority priority;
    private Boolean completed;
    private List<String> categories; // 기봅값 설정 X <- 요청이 없을 땐, null로 들어올 수 있음
}
