package com.lephuocviet.forum.controller;

import com.lephuocviet.forum.dto.responses.UploadResponse;
import com.lephuocviet.forum.exception.ApiResponses;
import com.lephuocviet.forum.service.IUploadService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping("/upload")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class UploadController {

    IUploadService uploadService;

    @PostMapping("/user")
    public ResponseEntity<ApiResponses<UploadResponse>> uploadImgUser(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponses.<UploadResponse>builder()
                .result(uploadService.uploadImgUser(file))
                .build());

    }

    @PostMapping("/post")
    public ResponseEntity<ApiResponses<UploadResponse>> uploadImgPost(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ApiResponses.<UploadResponse>builder()
                .result(uploadService.uploadImgPost(file))
                .build());

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponses<Boolean>> deletedImg(@PathVariable("id") String id ) throws IOException {
        return ResponseEntity.ok(ApiResponses.<Boolean>builder()
                .result(uploadService.deletedImg(id))
                .build());

    }

}
