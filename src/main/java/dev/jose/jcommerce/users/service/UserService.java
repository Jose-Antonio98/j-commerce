package dev.jose.jcommerce.users.service;

import dev.jose.jcommerce.exception.ResourceAlreadyRegistered;
import dev.jose.jcommerce.exception.ResourceNotFoundException;
import dev.jose.jcommerce.users.dto.UserRequest;
import dev.jose.jcommerce.users.dto.UserResponse;
import dev.jose.jcommerce.users.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly=true)
public class UserService  {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {

        duplicateEmailCheck(userRequest);

        String encodedPassword = passwordEncoder.encode(userRequest.password());

        try {
            var user = userRepository.saveAndFlush(userRequest.toEntity(encodedPassword));
            return UserResponse.fromEntity(user);
        } catch (DataIntegrityViolationException ex) {
            if (isEmailUniqueViolation(ex)) {
                throw new ResourceAlreadyRegistered("usuário");
            }
            throw ex;
        }
    }

    public UserResponse findUserById(UUID id) {
        return UserResponse.fromEntity(userRepository.findById(id).orElseThrow(ResourceNotFoundException::new));
    }

    public UserResponse findUserByEmail(String email) {
        return UserResponse.fromEntity(userRepository.findByEmail(email).orElseThrow(ResourceNotFoundException::new));
    }






    private void duplicateEmailCheck(UserRequest userRequest) {
        if (userRequest.email() != null && userRepository.existsByEmail(userRequest.email())) {

            throw new ResourceAlreadyRegistered("usuário");
        }
    }

    private boolean isEmailUniqueViolation(DataIntegrityViolationException ex) {
        Throwable cause = ex;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                return "users_email_key".equals(violation.getConstraintName());
            }
            cause = cause.getCause();
        }
        return false;
    }
}



