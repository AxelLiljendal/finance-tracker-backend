package com.github.axelliljendal.finance_tracker.repository;

import com.github.axelliljendal.finance_tracker.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public class UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
