package com.lephuocviet.forum.repository;

import com.lephuocviet.forum.enity.Translates;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranslatesRepository extends JpaRepository<Translates, String> {

    List<Translates> findTranslatesByLanguage_Id(String id);

}