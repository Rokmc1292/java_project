package com.example.backend.dto;

import lombok.Data;

@Data
public class AttachmentRequest {
    private String fileType;
    private String fileUrl;
    private String fileName;
}