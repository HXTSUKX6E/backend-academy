package backend.academy.linktracker.scrapper.repository.impl.orm.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_links")
@Getter
@Setter
@NoArgsConstructor
public class ChatLinkEntity {

    @EmbeddedId
    private ChatLinkKey id;

    public ChatLinkEntity(ChatLinkKey id) {
        this.id = id;
    }
}
