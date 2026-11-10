# 가계부 API

지출을 기록하고, 월 예산을 넘으면 알려주는 가계부 백엔드입니다.
계층(controller·service·dto·entity·repository)별로 폴더를 나누고, 그 안을 메뉴(지출·예산·리포트)로 나눴습니다.

## 기술 스택
- Java 17, Spring Boot 3.3, Spring Data JPA, H2(인메모리)
- 테스트: JUnit 5, AssertJ, ArchUnit

## 실행
```bash
./gradlew bootRun     # http://localhost:8080
./gradlew test        # 엔티티·서비스·API·아키텍처 테스트
```

## API
| 메서드 | 경로 | 설명 |
|---|---|---|
| `PUT` | `/api/budgets/{yyyy-MM}` | 월 예산 설정 `{"limit": 500000}` |
| `POST` | `/api/expenses` | 지출 등록 `{"amount": 12000, "category": "식비", "spentAt": "2026-10-15"}` (`spentAt` 생략 시 오늘) |
| `GET` | `/api/reports/{yyyy-MM}` | 월 합계, 남은 예산, 카테고리별 합계, 지출 목록 |

예산을 넘는 지출이 등록되면 응답의 `budgetExceeded`가 `true`가 되고, `BudgetAlertService`(현재는 로그)가 경고를 남깁니다.

## 패키지 구조
```
com.example.ledger
 ├─ controller   expense, budget, report
 ├─ service      expense, budget, report
 ├─ dto          expense, budget, report     ← 요청·응답·명령·결과
 ├─ entity       expense, budget             ← 규칙 검사 포함
 ├─ repository   expense, budget
 └─ common       config, exception
```
계층은 `controller → service → repository → entity` 방향, 메뉴는 `report → expense·budget`, `expense → budget` 방향이며, `ArchitectureTest`가 순환 의존을 막습니다. 자세한 설명은 [ARCHITECTURE.md](ARCHITECTURE.md)에 있습니다.
