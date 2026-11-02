# 가계부 API — 클린 아키텍처 적용

지출을 기록하고, 월 예산을 넘으면 알려주는 가계부 백엔드입니다.
클린 아키텍처(포트와 어댑터)로 핵심 규칙을 Spring·JPA에서 분리했습니다.

## 기술 스택
- Java 17, Spring Boot 3.3, Spring Data JPA, H2(인메모리)
- 테스트: JUnit 5, AssertJ, ArchUnit

## 실행
```bash
./gradlew bootRun     # http://localhost:8080
./gradlew test        # 도메인·유스케이스·아키텍처·API 테스트
```

## API
| 메서드 | 경로 | 설명 |
|---|---|---|
| `PUT` | `/api/budgets/{yyyy-MM}` | 월 예산 설정 `{"limit": 500000}` |
| `POST` | `/api/expenses` | 지출 등록 `{"amount": 12000, "category": "식비", "spentAt": "2026-10-15"}` (`spentAt` 생략 시 오늘) |
| `GET` | `/api/reports/{yyyy-MM}` | 월 합계, 남은 예산, 카테고리별 합계, 지출 목록 |

예산을 넘는 지출이 등록되면 응답의 `budgetExceeded`가 `true`가 되고, 알림 어댑터(현재는 로그)가 경고를 남깁니다.

## 패키지 구조
```
com.example.ledger
 ├─ domain                 Expense, Budget, MonthlyExpenses       ← 순수 Java
 ├─ application
 │   ├─ port.in            AddExpenseUseCase, SetBudgetUseCase, GetMonthlyReportUseCase
 │   ├─ port.out           SaveExpensePort, LoadExpensesPort, BudgetPort, BudgetAlertPort
 │   └─ service            유스케이스 구현 (Spring 어노테이션 없음)
 ├─ adapter
 │   ├─ in.web             컨트롤러, 예외 처리
 │   └─ out
 │       ├─ persistence    JPA 엔티티·리포지토리·포트 구현
 │       └─ notification   예산 초과 알림 (로그)
 └─ config                 UseCaseConfig — 유스케이스 빈 조립
```
의존 방향은 `adapter → application → domain` 한 방향이며, `ArchitectureTest`가 이를 검사합니다.
