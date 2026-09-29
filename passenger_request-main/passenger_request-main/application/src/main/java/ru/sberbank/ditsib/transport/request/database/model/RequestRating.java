package ru.sberbank.ditsib.transport.request.database.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity describing request
 */
@Embeddable
@Data
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestRating {
    
    /**
     * Collection of request advantages selected by user
     */
    @ElementCollection
    @CollectionTable(name = "advantages", schema = "request", joinColumns = @JoinColumn(name = "request_id"))
    @Builder.Default
    @Column(name = "value")
    private final Set<String> advantages = new HashSet<>();
    
    /**
     * Collection of request drawbacks selected by user
     */
    @ElementCollection
    @CollectionTable(name = "drawbacks", schema = "request", joinColumns = @JoinColumn(name = "request_id"))
    @Builder.Default
    @Column(name = "value")
    private final Set<String> drawbacks = new HashSet<>();
    
    /**
     * Rating
     */
    @Column(name = "rating_mark")
    private Integer rating;
    
    /**
     * Comment
     */
    @Column(name = "rating_comment")
    private String ratingComment;
}
