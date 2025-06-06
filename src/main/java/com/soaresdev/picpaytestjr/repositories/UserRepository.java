package com.soaresdev.picpaytestjr.repositories;

import com.soaresdev.picpaytestjr.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByCpfCnpj(String cpfCnpj);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}