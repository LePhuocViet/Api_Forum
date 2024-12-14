package com.lephuocviet.forum.repository;

import com.lephuocviet.forum.enity.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountsRepository extends JpaRepository<Accounts, String> {

    Optional<Accounts> findAccountsById(String id);

    Optional<Accounts> findAccountsByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsById(String id);

    @Query("SELECT CASE WHEN count(a) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Accounts a " +
            "LEFT JOIN a.roles r " +
            "WHERE r.name = 'ADMIN' ")
    boolean existsByUsernameRoleAdmin(String username);

}