package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class PostRequest {

    private Long categoryId;
    private String categoryName;

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 200, message = "제목은 최대 200자까지 입력 가능합니다.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    @Size(max = 7000, message = "내용은 최대 7000자까지 입력 가능합니다.")
    private String content;

    private List<AttachmentRequest> attachments;
}