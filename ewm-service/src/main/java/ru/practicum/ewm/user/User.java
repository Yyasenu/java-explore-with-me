package ru.practicum.ewm.user;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.request.Request;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", nullable = false, length = 512, unique = true)
    private String email;

    @OneToMany(mappedBy = "initiator", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Request> requests = new ArrayList<>();
}