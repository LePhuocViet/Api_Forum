package com.lephuocviet.forum.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum LanguageCode {
    ENGLISH("English"),
    CHINA("China"),
    JAPAN("Japan"),

    ;
    private String name;
}
