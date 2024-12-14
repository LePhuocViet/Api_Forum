package com.lephuocviet.forum.controller;

import com.lephuocviet.forum.dto.responses.ActiveResponse;
import com.lephuocviet.forum.exception.ApiResponses;
import com.lephuocviet.forum.service.IAccountService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/accounts")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class AccountsController {

    IAccountService accountService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponses<ActiveResponse>> checkActive() {
        return ResponseEntity.ok(ApiResponses.<ActiveResponse>builder()
                .result(accountService.checkActive())
                .build());
    }

}
