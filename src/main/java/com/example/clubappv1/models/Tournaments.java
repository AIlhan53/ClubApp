package com.example.clubappv1.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


/**
 * Entity representing a tournament organized by a club.
 *
 * <p>A tournament belongs to exactly one organizing {@link Clubs} and can
 * have several {@link Users} registered as participants.</p>
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "tournaments")
@ToString(exclude = {"users", "club"})
public class Tournaments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int Id;

    private String tournamentName;
    private String tournamentDescription;

    private boolean active;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime tournamentDate;

    /**
     * The club organizing this tournament.
     * <p>
     * A tournament belongs to exactly one club (many-to-one relationship).
     * Uses lazy fetching for performance optimization.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    @JoinColumn(name = "club_id", nullable = false)
    @JsonBackReference
    private Clubs club;

    /**
     * The users registered as participants of this tournament.
     */
    @ManyToMany(mappedBy = "tournaments")
    @JsonManagedReference
    @JsonIgnore
    private Set<Users> users = new HashSet<>();

}
