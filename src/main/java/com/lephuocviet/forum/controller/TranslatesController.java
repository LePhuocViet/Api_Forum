package com.lephuocviet.forum.controller;

import com.lephuocviet.forum.enity.Translates;
import com.lephuocviet.forum.exception.ApiResponses;
import com.lephuocviet.forum.service.ITranslatesService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/translates")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TranslatesController {
    ITranslatesService translatesService;

    @GetMapping("/{language}")
    public ResponseEntity<ApiResponses<List<Translates>>> getByLanguage(@PathVariable("language") String language) {
        return ResponseEntity.ok(ApiResponses.<List<Translates>>builder()
                .result(translatesService.getTranslatesByLanguage(language))
                .build());

    }
}
