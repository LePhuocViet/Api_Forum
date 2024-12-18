package com.lephuocviet.forum.service;

import com.lephuocviet.forum.enity.Translates;

import java.util.List;

public interface ITranslatesService {

    List<Translates> getTranslatesByLanguage(String language);

}
