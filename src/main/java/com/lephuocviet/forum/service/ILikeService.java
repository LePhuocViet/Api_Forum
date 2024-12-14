package com.lephuocviet.forum.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lephuocviet.forum.dto.requests.LikeRequest;
import com.lephuocviet.forum.dto.responses.LikeResponse;

public interface ILikeService {

    LikeResponse actionLike(LikeRequest likeRequest) throws JsonProcessingException;

}
