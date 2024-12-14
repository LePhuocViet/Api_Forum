package com.lephuocviet.forum.controller;

import com.lephuocviet.forum.dto.requests.AuthenticationRequest;
import com.lephuocviet.forum.dto.requests.IntrospectionRequest;
import com.lephuocviet.forum.dto.responses.AuthenticationResponse;
import com.lephuocviet.forum.dto.responses.IntrospectionResponse;
import com.lephuocviet.forum.exception.ApiResponses;
import com.lephuocviet.forum.service.IAuthService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class AuthenticationController {

    IAuthService iAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponses<AuthenticationResponse>> login(@RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(ApiResponses.<AuthenticationResponse>builder()
                .result(iAuthService.authentication(authenticationRequest))
                .build());
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponses<IntrospectionResponse>> introspect(@RequestBody IntrospectionRequest introspectionRequest) throws ParseException, JOSEException {
        return ResponseEntity.ok(ApiResponses.<IntrospectionResponse>builder()
                .result(iAuthService.introspection(introspectionRequest))
                .build());
    }


    @PostMapping("/refresh")
    public ResponseEntity<ApiResponses<AuthenticationResponse>> refresh(@RequestBody IntrospectionRequest introspectionRequest) throws ParseException, JOSEException {
        return ResponseEntity.ok(ApiResponses.<AuthenticationResponse>builder()
                .result(iAuthService.refreshToken(introspectionRequest))
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponses<AuthenticationResponse>> logout(@RequestBody IntrospectionRequest introspectionRequest) throws ParseException, JOSEException {
        return ResponseEntity.ok(ApiResponses.<AuthenticationResponse>builder()
                .result(iAuthService.logout(introspectionRequest))
                .build());
    }
}
