package backend.academy.linktracker.scrapper.repository.impl.orm.repository;

import backend.academy.linktracker.scrapper.repository.impl.orm.entity.LinkEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkJpaRepository extends JpaRepository<LinkEntity, Long> {

    Optional<LinkEntity> findByUrl(String url);

    List<LinkEntity> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);
}
