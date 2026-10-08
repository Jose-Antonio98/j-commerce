package dev.jose.jcommerce.users.repository;

import dev.jose.jcommerce.users.domain.UsersId;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, UsersId> {

    Optional<User> findByEmail(String email);
}
