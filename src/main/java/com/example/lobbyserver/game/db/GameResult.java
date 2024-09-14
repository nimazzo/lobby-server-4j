package com.example.lobbyserver.game.db;

import com.example.lobbyserver.user.db.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_result")
@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("unused")
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username", nullable = false, foreignKey = @ForeignKey(name = "fk_game_results_users"))
    private User user;

    @Column(nullable = false)
    private Long score;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private Long time;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    public GameResult(Long id, User user, Long score, Integer level, Long time, LocalDateTime dateTime) {
        this.id = id;
        this.user = new User(user);
        this.score = score;
        this.level = level;
        this.time = time;
        this.dateTime = dateTime;
    }

    public User getUser() {
        return new User(user);
    }

    public void setUser(User user) {
        this.user = new User(user);
    }
}
