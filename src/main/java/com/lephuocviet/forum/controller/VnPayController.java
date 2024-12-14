package com.lephuocviet.forum.controller;

import com.lephuocviet.forum.dto.responses.VnPayResponse;
import com.lephuocviet.forum.exception.ApiResponses;
import com.lephuocviet.forum.service.IVnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/vnpay")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class VnPayController {

    IVnPayService vnPayService;

    @GetMapping("/submitOrder")
    public ResponseEntity<ApiResponses<VnPayResponse>> create(HttpServletRequest request,
                                                              @RequestParam String location,
                                                              @RequestParam String type,
                                                              @RequestParam String idHandler,
                                                              @RequestParam String sales_package) {
        return ResponseEntity.ok(ApiResponses.<VnPayResponse>builder()
                .result(vnPayService.createOrder(request, location,type,idHandler,sales_package))
                .build());
    }

    @GetMapping("/orderReturn")
    public ResponseEntity<ApiResponses<VnPayResponse>> orderReturn(HttpServletRequest request) {
        VnPayResponse response = vnPayService.orderReturn(request);
        return ResponseEntity.ok(ApiResponses.<VnPayResponse>builder()
                .result(response)
                .build());
    }

}
