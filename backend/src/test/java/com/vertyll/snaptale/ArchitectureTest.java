package com.vertyll.snaptale;

import jakarta.persistence.Entity;

import org.springframework.data.repository.Repository;
import org.springframework.web.bind.annotation.RestController;

import com.vertyll.snaptale.keycloak.KeycloakAccounts;
import com.vertyll.snaptale.keycloak.KeycloakProperties;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.vertyll.snaptale", importOptions = ImportOption.DoNotIncludeTests.class)
final class ArchitectureTest {

    @ArchTest
    static final ArchRule CONTROLLERS_ARE_PACKAGE_PRIVATE =
            classes().that().areAnnotatedWith(RestController.class).should().notHaveModifier(JavaModifier.PUBLIC);

    @ArchTest
    static final ArchRule REPOSITORIES_ARE_PACKAGE_PRIVATE =
            classes().that().areAssignableTo(Repository.class).should().notHaveModifier(JavaModifier.PUBLIC);

    @ArchTest
    static final ArchRule ENTITIES_ARE_PACKAGE_PRIVATE =
            classes().that().areAnnotatedWith(Entity.class).should().notHaveModifier(JavaModifier.PUBLIC);

    @ArchTest
    static final ArchRule CONTROLLERS_DO_NOT_TALK_TO_REPOSITORIES = noClasses().that()
        .areAnnotatedWith(RestController.class)
        .should()
        .dependOnClassesThat()
        .areAssignableTo(Repository.class);

    @ArchTest
    static final ArchRule KEYCLOAK_IS_REACHED_THROUGH_ITS_ENTRY = noClasses().that()
        .resideOutsideOfPackage("com.vertyll.snaptale.keycloak..")
        .should()
        .dependOnClassesThat(
            resideInAPackage("com.vertyll.snaptale.keycloak..").and(not(equivalentTo(KeycloakAccounts.class)))
                .and(not(equivalentTo(KeycloakProperties.class)))
        );

    @ArchTest
    static final ArchRule MODULES_ARE_FREE_OF_CYCLES =
            slices().matching("com.vertyll.snaptale.(*)..").should().beFreeOfCycles();

    private ArchitectureTest() {
    }
}
