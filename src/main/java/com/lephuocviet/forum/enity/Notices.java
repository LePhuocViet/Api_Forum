package com.lephuocviet.forum.enity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "notices")
public class Notices {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String message;
    LocalDate date_created;
    boolean status;
    String idPost;
    String idComment;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    Users users;

}
