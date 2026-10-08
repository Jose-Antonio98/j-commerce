package dev.jose.jcommerce.users.domain;

import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;

import java.util.UUID;

@Embeddable
public record UsersId(UUID id) {

    public UsersId(UUID id) {
        Assert.notNull(id, "id must not be null");
        this.id = id;
    }

    public UsersId() {
        this(UUID.randomUUID());
    }
}
