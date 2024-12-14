package com.lephuocviet.forum.service.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lephuocviet.forum.dto.requests.CommentRequest;
import com.lephuocviet.forum.dto.responses.CommentResponse;
import com.lephuocviet.forum.enity.Comments;
import com.lephuocviet.forum.enity.Notices;
import com.lephuocviet.forum.enity.Posts;
import com.lephuocviet.forum.enity.Users;
import com.lephuocviet.forum.enums.ErrorCode;
import com.lephuocviet.forum.exception.WebException;
import com.lephuocviet.forum.mapper.CommentMapper;
import com.lephuocviet.forum.mapper.PostMapper;
import com.lephuocviet.forum.repository.CommentsRepository;
import com.lephuocviet.forum.repository.NoticesRepository;
import com.lephuocviet.forum.repository.PostsRepository;
import com.lephuocviet.forum.repository.UsersRepository;
import com.lephuocviet.forum.service.ICommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService implements ICommentService {
    SimpMessagingTemplate simpMessagingTemplate;

    CommentsRepository commentsRepository;

    UsersRepository usersRepository;

    PostsRepository postsRepository;

    CommentMapper commentMapper;

    NoticesRepository noticesRepository;
    @Override
    public CommentResponse createComment(CommentRequest commentRequest) throws JsonProcessingException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = usersRepository.findUserByUsername(username)
                .orElseThrow(() -> new WebException(ErrorCode.USER_NOT_FOUND));
        Posts posts = postsRepository.findPostsById(commentRequest.getId_post())
                .orElseThrow(() -> new WebException(ErrorCode.POST_NOT_FOUND));
        Comments comments = Comments.builder()
                .date_created(LocalDate.now())
                .content(commentRequest.getContent())
                .posts(posts)
                .users(user)
                .build();
        Integer commentCount = commentsRepository.countCommentsByPosts_Id(commentRequest.getId_post());

        String message = "";
        if (commentCount == null){
            message = message + user.getName() +" is comment your post " + posts.getTitle();
        } else {
            message = message + user.getName() +  " and "  +  commentCount + " user "   +" are comments your post " + posts.getTitle();
        }
        Notices resultNotice;
        Optional<Notices> noticesComment = noticesRepository.findByIdCommentAndIdPost(comments.getId(), commentRequest.getId_post());
        if (noticesComment.isEmpty()){
            Notices notices = Notices.builder()
                    .users(user)
                    .date_created(LocalDate.now())
                    .idComment(comments.getId())
                    .message(message)
                    .status(false)
                    .build();
            resultNotice = noticesRepository.save(notices);
        }else {
            noticesComment.get().setMessage(message);
            noticesComment.get().setDate_created(LocalDate.now());
            noticesComment.get().setStatus(false);
            resultNotice = noticesRepository.save(noticesComment.get());
        }
        if (posts.getUsers().getId() == user.getId()){
            return commentMapper.toCommentResponse(commentsRepository.save(comments));
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String resultNoticeJson = objectMapper.writeValueAsString(resultNotice);
        simpMessagingTemplate.convertAndSend("/topic/user/" + posts.getUsers().getId(),resultNoticeJson);
        return commentMapper.toCommentResponse(commentsRepository.save(comments));
    }

    @Override
    public  Page<CommentResponse> getCommentByPostId(String postId,Integer page, Integer size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users users = usersRepository.findUserByUsername(username)
                .orElseThrow(() -> new WebException(ErrorCode.USER_NOT_FOUND));
        Pageable pageable = PageRequest.of(page,size);
        Page<CommentResponse> list = commentsRepository.getCommentsByPosts_Id(postId,pageable);
        for (CommentResponse commentResponse : list){
            if (commentResponse.getId_user().equals(users.getId())){
                commentResponse.setUsers_comment(true);
            }
        }
        return list;
    }
}
