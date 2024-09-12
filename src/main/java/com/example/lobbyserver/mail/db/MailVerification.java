package com.example.lobbyserver.mail.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mail_verification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class MailVerification {

    @Id
    @Column(length = 36)
    private String token;

    @Column(length = 320, unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

}
