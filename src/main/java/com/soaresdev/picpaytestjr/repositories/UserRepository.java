package com.soaresdev.picpaytestjr.repositories;

import com.soaresdev.picpaytestjr.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}