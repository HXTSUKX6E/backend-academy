package backend.academy.linktracker.scrapper.repository.impl.orm.repository;

import backend.academy.linktracker.scrapper.repository.impl.orm.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatJpaRepository extends JpaRepository<ChatEntity, Long> {}
