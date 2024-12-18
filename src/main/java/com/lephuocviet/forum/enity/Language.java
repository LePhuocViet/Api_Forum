package com.lephuocviet.forum.enity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "language")
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    String description;

    @OneToMany(mappedBy = "language",cascade = CascadeType.ALL, orphanRemoval = true)
    List<Posts> posts;
    @OneToMany(mappedBy = "language", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Translates> translates;
}
