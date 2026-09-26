package com.vertyll.snaptale.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

interface UserTokenRepository extends JpaRepository<UserTokenEntity, Long> {

    Optional<UserTokenEntity> findByTokenHashAndPurpose(String tokenHash, TokenPurpose purpose);

    @Modifying
    @Query("DELETE FROM UserTokenEntity t WHERE t.userId = :userId AND t.purpose = :purpose")
    void deleteByUserIdAndPurpose(long userId, TokenPurpose purpose);
}
