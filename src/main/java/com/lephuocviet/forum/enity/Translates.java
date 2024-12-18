package com.lephuocviet.forum.enity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "translates")
public class Translates {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String keyName;

    String translated;

    @ManyToOne
    @JoinColumn(name = "language_id")
    @JsonIgnore
    Language language;


}
