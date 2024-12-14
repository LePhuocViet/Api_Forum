package com.lephuocviet.forum.service;

import com.lephuocviet.forum.dto.responses.NoticeResponse;

import java.util.List;

public interface INoticeService {

    List<NoticeResponse> updateNoticeByIdUser(String idUser);

    List<NoticeResponse> getAllNoticeByIdUser(String idUser,int page, int size);
}
