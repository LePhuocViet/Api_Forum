package com.lephuocviet.forum.service.implement;
import java.util.Optional;
import com.lephuocviet.forum.dto.requests.PostRequest;
import com.lephuocviet.forum.dto.responses.PostResponse;
import com.lephuocviet.forum.enity.Language;
import com.lephuocviet.forum.enity.Posts;
import com.lephuocviet.forum.enity.Users;
import com.lephuocviet.forum.enums.ErrorCode;
import com.lephuocviet.forum.enums.RolesCode;
import com.lephuocviet.forum.exception.WebException;
import com.lephuocviet.forum.mapper.PostMapper;
import com.lephuocviet.forum.repository.*;
import com.lephuocviet.forum.service.IPostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService implements IPostService {
    PostsRepository postsRepository;
    UsersRepository usersRepository;
    AccountsRepository accountsRepository;
    LanguageRepository languageRepository;
    LikesRepository likesRepository;
    PostMapper postMapper;

    @Override
    public PostResponse createPost(PostRequest postRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Posts posts = postMapper.toPosts(postRequest);
        Users users = usersRepository.findUserByUsername(username)
                .orElseThrow(() -> new WebException(ErrorCode.USER_NOT_FOUND));
        posts.setUsers(users);
        posts.setDate_created(LocalDate.now());
        Language language = languageRepository.findByName(postRequest.getLanguage())
                        .orElseThrow(() -> new WebException(ErrorCode.LANGUAGE_NOT_FOUND));
        posts.setLanguage(language);

        return postMapper.toPostPageResponse(postsRepository.save(posts));
    }

    @Override
    public Page<PostResponse> getPosts(Integer page, Integer size, String language, String content) {
        if (SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser")){
            Pageable pageable =  PageRequest.of(page,size);
            Page<PostResponse> postPageResponseList = postsRepository.getPostPage(content,language,null,pageable);
            if (postPageResponseList.isEmpty()) throw new WebException(ErrorCode.POST_NOT_FOUND);
            return postPageResponseList;
        } else {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Users users = usersRepository.findUserByUsername(username)
                    .orElseThrow(() -> new WebException(ErrorCode.USER_NOT_FOUND));
            Pageable pageable = PageRequest.of(page,size);
            Page<PostResponse> postPageResponseList = postsRepository.getPostPage(content,language,users.getId(),pageable);
            for (PostResponse post : postPageResponseList){
                if (likesRepository.existsByPosts_IdAndUsers_Id(post.getId(), users.getId())){
                    post.setUser_like(true);
                }
            }
            return postPageResponseList;
        }
    }

    @Override
    public PostResponse getPostById(String id) {
        if (!postsRepository.existsPostsById(id)) throw new WebException(ErrorCode.POST_NOT_FOUND);
        if (SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser")){
            PostResponse postResponse = postsRepository.getPostById(id,null);
            return postResponse;
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users users = usersRepository.findUserByUsername(username)
                .orElseThrow(() -> new WebException(ErrorCode.USER_NOT_FOUND));

        PostResponse postResponse = postsRepository.getPostById(id,users.getId());
        if (likesRepository.existsByPosts_IdAndUsers_Id(postResponse.getId(), users.getId())){
            postResponse.setUser_like(true);
        }
        return postResponse;
    }

    @Override
    public Page<PostResponse> getPostsByUserId(String userId, Integer page, Integer size) {
       Pageable pageable =  PageRequest.of(page,size);
       String username = SecurityContextHolder.getContext().getAuthentication().getName();
       String id = "";
       if (username.equals("anonymousUser")){
           id =  9999 + "";
       } else {
           Optional<Users> myUser = usersRepository.findUserByUsername(username);
           id = myUser.get().getId();
       }
       Users users = usersRepository.findUsersById(userId)
               .orElseThrow(() -> new WebException(ErrorCode.USER_NOT_FOUND));
       Page<PostResponse> postResponses = postsRepository.getPostPageByUserId(users.getId(),id,pageable);
       if (postResponses.isEmpty()) throw new WebException(ErrorCode.POST_NOT_FOUND);
       for (PostResponse post : postResponses){
           if (likesRepository.existsByPosts_IdAndUsers_Id(post.getId(),id)){
               post.setUser_like(true);
           }
       }
       return postResponses;
    }

    @Override
    public void deletePostById(String id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users users = usersRepository.findUserByUsername(username)
                .orElseThrow(() -> new WebException(ErrorCode.USER_NOT_FOUND));

        Posts posts = postsRepository.findPostsById(id).orElseThrow(() -> new WebException(ErrorCode.POST_NOT_FOUND));

        if (users.getId().equals(posts.getUsers().getId()) ||
                accountsRepository.existsByUsernameRoleAdmin(users.getAccounts().getUsername())){
            postsRepository.delete(posts);
        } else {
            throw new WebException(ErrorCode.NOT_USER_POST);
        }

    }
}
