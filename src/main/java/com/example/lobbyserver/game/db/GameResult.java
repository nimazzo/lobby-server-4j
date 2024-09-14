package com.example.lobbyserver.game.db;

import com.example.lobbyserver.user.db.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
