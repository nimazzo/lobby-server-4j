package com.example.lobbyserver.user.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "authorities", indexes = @Index(name = "ix_auth_username", columnList = "username,authority", unique = true))
@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("unused")
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username", nullable = false, foreignKey = @ForeignKey(name = "fk_authorities_users"))
    private User user;

    @Column(nullable = false, length = 50)
    private String authority;

    public Authority(Long id, User user, String authority) {
        this.id = id;
        this.user = new User(user);
        this.authority = authority;
    }

    public User getUser() {
        return new User(user);
    }

    public void setUser(User user) {
        this.user = new User(user);
    }
}
