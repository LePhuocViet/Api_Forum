package com.lephuocviet.forum.service.implement;

import com.lephuocviet.forum.enity.Language;
import com.lephuocviet.forum.enity.Translates;
import com.lephuocviet.forum.enums.ErrorCode;
import com.lephuocviet.forum.exception.WebException;
import com.lephuocviet.forum.repository.LanguageRepository;
import com.lephuocviet.forum.repository.TranslatesRepository;
import com.lephuocviet.forum.service.ITranslatesService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class TranslateService implements ITranslatesService {

    LanguageRepository languageRepository;

    TranslatesRepository translatesRepository;

    @Override
    public List<Translates> getTranslatesByLanguage(String language) {
        Language object = languageRepository
                .findByName(language)
                .orElseThrow(() -> new  WebException(ErrorCode.LANGUAGE_NOT_FOUND));
        List<Translates> translates = translatesRepository.findTranslatesByLanguage_Id(object.getId());
        return translates;
    }
}
