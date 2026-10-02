package com.vertyll.snaptale.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;

interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByKeycloakId(String keycloakId);

    @NativeQuery("SELECT * FROM users WHERE id <> :excludedId ORDER BY RAND() LIMIT :limit")
    List<UserEntity> findRandom(long excludedId, int limit);
}
