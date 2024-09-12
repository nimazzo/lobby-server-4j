package com.example.lobbyserver.mail.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MailVerificationRepository extends JpaRepository<MailVerification, String> {
}
