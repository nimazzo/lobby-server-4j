package com.example.lobbyserver.user.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, String> {
    @Transactional
    @Modifying
    @Query("update User u set u.email = ?1 where u.username = ?2")
    void updateEmailByUsername(@NonNull String email, String username);

    boolean existsByEmail(String email);

    @Transactional
    @Modifying
    @Query("update User u set u.enabled = ?1 where u.email = ?2")
    void updateEnabledByEmail(boolean enabled, String email);
}
