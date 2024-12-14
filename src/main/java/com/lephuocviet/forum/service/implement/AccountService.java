package com.lephuocviet.forum.service.implement;

import com.lephuocviet.forum.dto.requests.IntrospectionRequest;
import com.lephuocviet.forum.dto.responses.ActiveResponse;
import com.lephuocviet.forum.enity.Accounts;
import com.lephuocviet.forum.enums.ErrorCode;
import com.lephuocviet.forum.exception.WebException;
import com.lephuocviet.forum.repository.AccountsRepository;
import com.lephuocviet.forum.service.IAccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountService implements IAccountService {

    AccountsRepository accountsRepository;

    @Override
    public ActiveResponse checkActive() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Accounts accounts = accountsRepository.findAccountsByUsername(username)
                .orElseThrow(() -> new WebException(ErrorCode.USERNAME_NOT_FOUND));
        if (accounts.isActive()) return ActiveResponse.builder()
                .active(true)
                .message("Account is active")
                .build();
        else return ActiveResponse.builder()
                .active(false)
                .message("Account is not active")
                .build();
    }
}
