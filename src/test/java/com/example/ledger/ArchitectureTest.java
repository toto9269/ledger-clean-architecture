package com.example.ledger;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * 의존성 규칙을 말이 아니라 테스트로 지킨다.
 * 누군가 도메인에 @Entity 를 붙이거나 유스케이스에서 JPA 를 부르면 빌드가 실패한다.
 */
class ArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example.ledger");

    @Test
    void 도메인은_다른_계층과_프레임워크를_모른다() {
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application..", "..adapter..", "..config..",
                        "org.springframework..", "jakarta.persistence..")
                .check(classes);
    }

    @Test
    void 유스케이스는_어댑터와_프레임워크를_모른다() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..adapter..", "..config..",
                        "org.springframework..", "jakarta.persistence..")
                .check(classes);
    }

    @Test
    void 패키지_사이에_순환_의존이_없다() {
        slices().matching("com.example.ledger.(**)")
                .should().beFreeOfCycles()
                .check(classes);
    }
}
