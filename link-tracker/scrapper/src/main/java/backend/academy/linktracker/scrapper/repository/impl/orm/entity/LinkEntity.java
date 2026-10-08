package backend.academy.linktracker.scrapper.repository.impl.orm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "links")
@Getter
@Setter
@NoArgsConstructor
public class LinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false, unique = true)
    private String url;

    @Column(name = "tags", nullable = false)
    private String tags;

    @Column(name = "filters_data", nullable = false)
    private String filters;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;
}
