package com.lephuocviet.forum.repository;

import com.lephuocviet.forum.dto.responses.NoticeResponse;
import com.lephuocviet.forum.enity.Notices;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoticesRepository extends JpaRepository<Notices, String> {

    Optional<Notices> findByIdPost(String postId);
    Optional<Notices> findByIdCommentAndIdPost(String commentId,String postId);
    @Query("SELECT n " +
            "FROM Notices n " +
            "LEFT JOIN n.users u " +
            "WHERE n.status = false AND u.id = :id ")
    List<Notices> findNoticesNoRead(@Param("id") String id_user);

    @Query("SELECT NEW com.lephuocviet.forum.dto.responses.NoticeResponse( " +
            "n.id, n.message, n.date_created, n.status, n.idPost, n.idComment)" +
            "From Notices n " +
            "left Join n.users u " +
            "WHERE :id = u.id " +
            "ORDER BY n.date_created ASC")
    List<NoticeResponse> findNoticesByUsers_Id(@Param("id") String id, Pageable pageable);
}

