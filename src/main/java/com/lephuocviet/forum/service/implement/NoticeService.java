package com.lephuocviet.forum.service.implement;

import com.lephuocviet.forum.dto.responses.NoticeResponse;
import com.lephuocviet.forum.enity.Notices;
import com.lephuocviet.forum.enity.Users;
import com.lephuocviet.forum.enums.ErrorCode;
import com.lephuocviet.forum.exception.WebException;
import com.lephuocviet.forum.mapper.NoticeMapper;
import com.lephuocviet.forum.repository.NoticesRepository;
import com.lephuocviet.forum.repository.UsersRepository;
import com.lephuocviet.forum.service.INoticeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class NoticeService implements INoticeService {

    NoticesRepository noticesRepository;
    UsersRepository usersRepository;
    NoticeMapper noticeMapper;

    @Override
    public List<NoticeResponse> updateNoticeByIdUser(String idUser) {
        Users user = usersRepository.findUsersById(idUser)
                .orElseThrow(() -> new WebException(ErrorCode.USER_NOT_FOUND));
        List<Notices> noticesList = noticesRepository.findNoticesNoRead(user.getId());
        List<NoticeResponse> noticeResponseList = new ArrayList<>();
        for (Notices notices : noticesList) {
            notices.setStatus(true);
            noticesRepository.save(notices);
            noticeResponseList.add(noticeMapper.ToNoticeResponse(notices));
        }

        return noticeResponseList;
    }

    @Override
    public List<NoticeResponse> getAllNoticeByIdUser(String idUser, int page, int size) {
        Pageable pageable =  PageRequest.of(page,size);
        List<NoticeResponse> list = noticesRepository.findNoticesByUsers_Id(idUser,pageable);
        return list;
    }
}
