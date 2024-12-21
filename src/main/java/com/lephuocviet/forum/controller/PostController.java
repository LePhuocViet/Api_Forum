package com.lephuocviet.forum.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lephuocviet.forum.dto.requests.PostRequest;
import com.lephuocviet.forum.dto.responses.PostResponse;
import com.lephuocviet.forum.dto.responses.UserResponse;
import com.lephuocviet.forum.exception.ApiResponses;
import com.lephuocviet.forum.service.IPostService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/posts")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class PostController {

    IPostService postService;

    @PostMapping
    ResponseEntity<ApiResponses<PostResponse>> createPost(@RequestBody PostRequest postRequest) throws JsonProcessingException {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponses.<PostResponse>builder()
                .result(postService.createPost(postRequest))
                .build());

    }

    @GetMapping
    ResponseEntity<ApiResponses<Page<PostResponse>>> getAllPosts(@RequestParam(defaultValue = "") String content,
                                                                 @RequestParam(defaultValue = "") String language,
                                                                 @RequestParam(defaultValue = "0") Integer page,
                                                                 @RequestParam(defaultValue = "5") Integer size
    ) {
        return ResponseEntity.ok(ApiResponses.<Page<PostResponse>>builder()
                .result(postService.getPosts(page, size, language, content))
                .build());
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponses<PostResponse>> getPostById(@PathVariable("id") String id) {
        return ResponseEntity.ok(ApiResponses.<PostResponse>builder()
                .result(postService.getPostById(id))
                .build());
    }

    @GetMapping("/user/{id}")
    ResponseEntity<ApiResponses<Page<PostResponse>>> getUserById(@PathVariable("id") String id,@RequestParam(defaultValue = "0") Integer page ,@RequestParam(defaultValue = "0") Integer size) {
        return ResponseEntity.ok(ApiResponses.<Page<PostResponse>>builder()
                .result(postService.getPostsByUserId(id,page,size))
                .build());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deletePost(@PathVariable("id") String id) {
        postService.deletePostById(id);
        return ResponseEntity.noContent().build();
    }


}
