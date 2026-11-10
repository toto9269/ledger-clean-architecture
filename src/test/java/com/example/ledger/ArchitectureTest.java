package com.example.ledger;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * 의존 규칙을 테스트로 지킨다. 순환 의존이 생기면 빌드가 실패한다.
 * 계층 방향: controller → service → repository → entity, dto 는 controller·service 가 함께 쓴다.
 * 메뉴 방향: report → expense·budget, expense → budget.
 */
class ArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example.ledger");

    @Test
    void 계층_사이에_순환_의존이_없다() {
        slices().matching("com.example.ledger.(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }

    @Test
    void 메뉴_사이에_순환_의존이_없다() {
        slices().matching("com.example.ledger.*.(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }
}
