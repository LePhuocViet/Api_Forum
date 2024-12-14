package com.lephuocviet.forum.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lephuocviet.forum.dto.requests.CommentRequest;
import com.lephuocviet.forum.dto.responses.CommentResponse;
import com.lephuocviet.forum.exception.ApiResponses;
import com.lephuocviet.forum.service.ICommentService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/comments")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class CommentController {
    ICommentService commentService;

    @PostMapping
    ResponseEntity<ApiResponses<CommentResponse>> createComment(@RequestBody CommentRequest commentRequest) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponses.<CommentResponse>builder()
                .result(commentService.createComment(commentRequest))
                .build());
    }

    @GetMapping
    ResponseEntity<ApiResponses<Page<CommentResponse>>> getComment(@RequestParam(defaultValue = "") String id_post,
                                                                   @RequestParam(defaultValue = "0") Integer page ,
                                                                   @RequestParam(defaultValue = "5") Integer size ) {
        return ResponseEntity.ok(ApiResponses.<Page<CommentResponse>>builder()
                        .result(commentService.getCommentByPostId(id_post,page,size))
                .build());
    }
}
