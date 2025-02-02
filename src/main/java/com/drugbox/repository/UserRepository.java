package com.drugbox.repository;

import com.drugbox.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);
    Optional<User> findByEmail(String email);
    User findByOauthId(String oauthId);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
}
