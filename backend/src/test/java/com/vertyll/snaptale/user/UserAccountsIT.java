package com.vertyll.snaptale.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.vertyll.snaptale.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class UserAccountsIT {

    @Autowired
    UserAccounts accounts;

    @Autowired
    UserDirectory directory;

    @Test
    void firstSignInCreatesTheAccountAndLaterOnesFollowTheEmail() {
        long id = accounts.signIn("2f1c8b9e-6a8d-4f4e-9d65-1f1b0a7c3e21", "ala@example.com", "  Ala Kowalska  ");

        long again = accounts.signIn("2f1c8b9e-6a8d-4f4e-9d65-1f1b0a7c3e21", "ala.nowa@example.com", "Inna Nazwa");

        assertThat(again).isEqualTo(id);
        UserEntity user = directory.get(id);
        assertThat(user.getName()).isEqualTo("Ala Kowalska");
        assertThat(user.getEmail()).isEqualTo("ala.nowa@example.com");
    }

    @Test
    void aLongDisplayNameIsCutToTheLimit() {
        long id = accounts.signIn("8c0b6a41-3f0f-4d9b-b8d5-2a6f8e1c4b70", "dlugi@example.com", "x".repeat(80));

        assertThat(directory.get(id).getName()).hasSize(50);
    }
}
