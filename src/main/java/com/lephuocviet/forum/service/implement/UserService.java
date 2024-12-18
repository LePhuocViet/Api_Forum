package com.lephuocviet.forum.service.implement;

import com.lephuocviet.forum.dto.requests.UserRequest;
import com.lephuocviet.forum.dto.responses.UserResponse;
import com.lephuocviet.forum.enity.Accounts;
import com.lephuocviet.forum.enity.Roles;
import com.lephuocviet.forum.enity.Users;
import com.lephuocviet.forum.enums.ErrorCode;
import com.lephuocviet.forum.enums.LanguageCode;
import com.lephuocviet.forum.enums.RolesCode;
import com.lephuocviet.forum.exception.WebException;
import com.lephuocviet.forum.mapper.UserMapper;
import com.lephuocviet.forum.repository.AccountsRepository;
import com.lephuocviet.forum.repository.RolesRepository;
import com.lephuocviet.forum.repository.UsersRepository;
import com.lephuocviet.forum.service.IUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.catalina.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal=true)
public class UserService implements IUserService {

    UsersRepository usersRepository;

    AccountsRepository accountsRepository;

    PasswordEncoder passwordEncoder;
    RolesRepository rolesRepository;
    UserMapper userMapper;
    @Override
    public UserResponse createUser(UserRequest userRequest) {
        if (accountsRepository.existsByUsername(userRequest.getUsername()))
            throw new WebException(ErrorCode.USERNAME_IS_EXISTS);
        if (usersRepository.existsByEmail(userRequest.getEmail()))
            throw new WebException(ErrorCode.EMAIL_IS_EXISTS);
        if (!userRequest.getPassword().equals(userRequest.getRepassword()))
            throw new WebException(ErrorCode.PASSWORD_NOT_MATCH);
        Set<Roles> set = new HashSet<>();
        set.add(rolesRepository.findRolesByName(RolesCode.USER.toString())
                .orElseThrow(() -> new WebException(ErrorCode.ROLE_NOT_FOUND)));
        Accounts accounts = Accounts.builder()
                .username(userRequest.getUsername())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .active(false)
                .status(true)
                .roles(set)
                .build();
        accountsRepository.save(accounts);
        Users user = userMapper.toUser(userRequest);
        user.setAccounts(accounts);
        usersRepository.save(user);
        return  userMapper.toUserResponses(user);
    }

    @Override
    public UserResponse updateUser(UserRequest userRequest) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users myUser = usersRepository.findUserByUsername(username)
                .orElseThrow(() -> new WebException(ErrorCode.USER_NOT_FOUND));
        userMapper.toUpdate(myUser,userRequest);
        usersRepository.save(myUser);
        return userMapper.toUserResponses(myUser);

    }

    @Override
    public void deleteUser(String userId) {
        Users user = usersRepository.findUsersById(userId)
                .orElseThrow(() -> new WebException(ErrorCode.USER_NOT_FOUND));
        usersRepository.delete(user);

    }

    @Override
    public UserResponse getUserById(String userId) {
       Users user = usersRepository.findUsersById(userId)
               .orElseThrow(() -> new WebException(ErrorCode.USER_NOT_FOUND));
       String username = SecurityContextHolder.getContext().getAuthentication().getName();

       UserResponse userResponse = userMapper.toUserResponses(user);
       if (user.getAccounts().getUsername().equals(username)){
           userResponse.setUser(true);
       }else userResponse.setUser(false);

        return userResponse;

    }

    @Override
    public List<UserResponse> findUser(String name) {
//        if (name.isEmpty())
//            return userMapper.toListUserResponses(usersRepository.findAll());
//        String textname = name.toLowerCase().trim();
//        List<Users> users = usersRepository.findUsersByName(textname);
//        if (users == null) throw new WebException(ErrorCode.USER_NOT_FOUND);
//        return userMapper.toListUserResponses(users);
        return null;
    }

    @Override
    public UserResponse getMyInformation() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users users = usersRepository.findUserByUsername(username)
                .orElseThrow(() -> new WebException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponses(users);
    }
}
