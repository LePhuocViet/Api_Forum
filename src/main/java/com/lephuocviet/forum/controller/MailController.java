package com.lephuocviet.forum.controller;

import com.lephuocviet.forum.dto.requests.MailRequest;
import com.lephuocviet.forum.dto.requests.PasswordRequest;
import com.lephuocviet.forum.dto.responses.MailResponse;
import com.lephuocviet.forum.exception.ApiResponses;
import com.lephuocviet.forum.service.IMailService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@AllArgsConstructor
@RequestMapping("/mail")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class MailController {

    IMailService iMailService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponses<MailResponse>> send() {
        return ResponseEntity.ok(ApiResponses.<MailResponse>builder()
                .result(iMailService.sendMailActive())
                .build());
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponses<MailResponse>> reset(@RequestBody MailRequest mailRequest) {
        return ResponseEntity.ok(ApiResponses.<MailResponse>builder()
                .result(iMailService.sendMailResetPassword(mailRequest))
                .build());
    }


    @PostMapping("/change")
    public ResponseEntity<ApiResponses<MailResponse>> active(@RequestParam String token, @RequestBody PasswordRequest passwordRequest) throws ParseException, JOSEException {
        return ResponseEntity.ok(ApiResponses.<MailResponse>builder()
                .result(iMailService.resetPassword(token,passwordRequest))
                .build());
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponses<MailResponse>> active(@RequestParam String token) throws ParseException, JOSEException {
        return ResponseEntity.ok(ApiResponses.<MailResponse>builder()
                .result(iMailService.checkMailActive(token))
                .build());
    }
}
