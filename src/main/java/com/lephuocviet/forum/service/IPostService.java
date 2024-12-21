package com.lephuocviet.forum.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lephuocviet.forum.dto.requests.PostRequest;
import com.lephuocviet.forum.dto.responses.PostResponse;
import org.springframework.data.domain.Page;

public interface IPostService {

    PostResponse createPost(PostRequest postRequest) throws JsonProcessingException;

    Page<PostResponse> getPosts(Integer page, Integer size, String language, String content);

    PostResponse getPostById(String id);

    Page<PostResponse> getPostsByUserId(String userId, Integer page, Integer size);

    void deletePostById(String id);
}
