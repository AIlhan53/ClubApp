package com.example.clubappv1.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a user role.
 *
 * <p>Wraps a {@link RoleType} (member, responsible, or admin) and tracks
 * every {@link Users} currently assigned to it.</p>
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "roles")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "users")
public class Roles {
    @Id
    @GeneratedValue
    private int id;
    @Enumerated(EnumType.STRING)
    private RoleType name;

    /**
     * The users currently assigned to this role.
     */
    @OneToMany(mappedBy = "role")
    @JsonIgnore
    private List<Users> users = new ArrayList<>();
}