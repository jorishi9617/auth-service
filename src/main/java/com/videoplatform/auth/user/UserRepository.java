package com.videoplatform.auth.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    @Modifying
    @Query(value = """
            INSERT INTO users (id, email, password_hash)
            VALUES (:id, :email, :passwordHash)
            ON CONFLICT (email) DO NOTHING
            """, nativeQuery = true)
    void createLocalTestUserIfAbsent(@Param("id") UUID id, @Param("email") String email,
                                     @Param("passwordHash") String passwordHash);
}
