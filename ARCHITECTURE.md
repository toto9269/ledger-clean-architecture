# 소스 구조 안내

가계부 API의 소스를 패키지별로 정리한 문서입니다. 각 패키지에 어떤 파일이 있고 무슨 일을 하는지 설명합니다.

## 전체 구조

```
화면(index.html) ─HTTP→ adapter.in.web ─→ application.port.in (입력 포트: 앱이 하는 일)
                                              │
                                     application.service (구현)
                                              │ 사용
                          domain (규칙)   application.port.out (출력 포트: 저장·알림)
                                              ↑ 구현
                               adapter.out.persistence / notification
config: 유스케이스 구현에 포트 구현체를 넣어 조립
```

- 의존 방향은 `adapter → application → domain` 한 방향뿐입니다.
- 도메인과 유스케이스는 Spring과 JPA를 모릅니다. [ArchitectureTest](src/test/java/com/example/ledger/ArchitectureTest.java)가 이 규칙을 검사하므로, 규칙을 어기면 빌드가 실패합니다.

---

## 1. domain: 비즈니스 규칙 (순수 Java)

`src/main/java/com/example/ledger/domain/`

| 파일 | 역할 | 지키는 규칙 |
|---|---|---|
| [Expense.java](src/main/java/com/example/ledger/domain/Expense.java) | 지출 한 건 (금액, 카테고리, 날짜, 메모) | 금액은 0보다 큼, 카테고리와 날짜는 필수, 메모는 100자 이하이고 비어 있으면 null |
| [ExpenseImage.java](src/main/java/com/example/ledger/domain/ExpenseImage.java) | 지출에 붙이는 이미지 한 장 | JPG·PNG·GIF·WEBP만, 장당 5MB 이하, 빈 파일 거부, 지출당 5장까지(`checkCount`) |
| [Budget.java](src/main/java/com/example/ledger/domain/Budget.java) | 한 달 예산 | 예산은 0보다 큼. 예산 초과 여부(`isExceededBy`)와 남은 금액(`remaining`)을 계산 |
| [MonthlyExpenses.java](src/main/java/com/example/ledger/domain/MonthlyExpenses.java) | 한 달 지출 묶음 | 다른 달 지출은 섞일 수 없음. 월 합계와 카테고리별 합계를 계산 |

규칙을 어기면 `IllegalArgumentException`을 던집니다. 웹 계층이 이 예외를 `400` 응답으로 바꿉니다.

---

## 2. application.port.in: 입력 포트 (유스케이스)

`src/main/java/com/example/ledger/application/port/in/`

이 앱이 할 수 있는 일(유스케이스)의 목록입니다. 각 파일은 인터페이스라서 무엇을 하는지만 정하고, 실제 동작은 4번 `service`가 구현합니다. 웹 같은 바깥 계층은 구현 클래스가 아니라 이 인터페이스만 보고 호출합니다.

| 파일 | 하는 일 | 주고받는 데이터 |
|---|---|---|
| [AddExpenseUseCase](src/main/java/com/example/ledger/application/port/in/AddExpenseUseCase.java) | 지출 등록 | `Command`(금액, 카테고리, 날짜, 메모, 이미지 목록)를 받아 `Result`(지출 id, 월 합계, 예산 초과 여부)를 돌려줌 |
| [SetBudgetUseCase](src/main/java/com/example/ledger/application/port/in/SetBudgetUseCase.java) | 월 예산 설정 | 월과 금액 |
| [GetMonthlyReportUseCase](src/main/java/com/example/ledger/application/port/in/GetMonthlyReportUseCase.java) | 월 리포트 조회 | `Report`(합계, 예산, 남은 예산, 카테고리별 합계, 지출 목록). 지출마다 메모와 이미지 id 목록 포함 |
| [GetExpenseImageUseCase](src/main/java/com/example/ledger/application/port/in/GetExpenseImageUseCase.java) | 이미지 한 장 조회 | 이미지 id를 받아 형식과 내용을 돌려줌 |
| [DeleteExpensesUseCase](src/main/java/com/example/ledger/application/port/in/DeleteExpensesUseCase.java) | 지출 여러 건 삭제 (딸린 이미지 포함) | 지출 id 목록을 받아 지운 건수를 돌려줌. 없는 지출이 하나라도 섞이면 아무것도 지우지 않음 |

---

## 3. application.port.out: 출력 포트

`src/main/java/com/example/ledger/application/port/out/`

유스케이스가 저장이나 알림처럼 혼자 할 수 없는 일을 바깥에 요청할 때 쓰는 인터페이스입니다. 실제 구현은 6번 어댑터가 맡으므로, DB나 알림 수단을 바꿔도 유스케이스 코드는 그대로 둡니다.

| 파일 | 하는 일 |
|---|---|
| [SaveExpensePort](src/main/java/com/example/ledger/application/port/out/SaveExpensePort.java) | 지출 저장 (저장 후 id가 채워진 지출을 돌려줌) |
| [LoadExpensesPort](src/main/java/com/example/ledger/application/port/out/LoadExpensesPort.java) | 한 달 지출 조회 |
| [DeleteExpensePort](src/main/java/com/example/ledger/application/port/out/DeleteExpensePort.java) | 지출 여러 건 삭제 (실제로 지운 건수를 돌려줌) |
| [BudgetPort](src/main/java/com/example/ledger/application/port/out/BudgetPort.java) | 예산 저장과 조회 |
| [ExpenseImagePort](src/main/java/com/example/ledger/application/port/out/ExpenseImagePort.java) | 이미지 저장, 지출별 이미지 id 조회(이미지 내용은 읽지 않음), 이미지 한 장 조회, 지출에 딸린 이미지 삭제 |
| [BudgetAlertPort](src/main/java/com/example/ledger/application/port/out/BudgetAlertPort.java) | 예산 초과 알림 (로그, 메일 등 수단은 어댑터가 정함) |

---

## 4. application.service: 유스케이스 구현

`src/main/java/com/example/ledger/application/service/`

서비스 클래스에는 `@Service`를 붙이지 않습니다. Spring을 모르고, 빈 등록은 `config`가 합니다.

| 파일 | 흐름 |
|---|---|
| [AddExpenseService](src/main/java/com/example/ledger/application/service/AddExpenseService.java) | ① 지출과 이미지를 먼저 모두 검증 → ② 지출 저장 → ③ 이미지 저장 → ④ 월 합계 계산 → ⑤ 예산을 넘었으면 알림 포트 호출. 이미지가 하나라도 잘못되면 지출도 저장하지 않음 |
| [SetBudgetService](src/main/java/com/example/ledger/application/service/SetBudgetService.java) | `Budget`을 만들어(규칙 검사) 저장 |
| [GetMonthlyReportService](src/main/java/com/example/ledger/application/service/GetMonthlyReportService.java) | 월 지출, 예산, 이미지 id를 모아 `Report`로 만듦 |
| [GetExpenseImageService](src/main/java/com/example/ledger/application/service/GetExpenseImageService.java) | 포트로 이미지를 읽어 돌려줌 |
| [DeleteExpensesService](src/main/java/com/example/ledger/application/service/DeleteExpensesService.java) | ① 빈 목록이면 거부 → ② 딸린 이미지를 먼저 삭제(고아 행 방지) → ③ 지출 삭제 → ④ 지운 건수가 요청과 다르면 예외. 트랜잭션 안에서 실행되므로 예외가 나면 ②·③이 모두 되돌려짐 |

---

## 5. adapter.in.web: HTTP 입구

`src/main/java/com/example/ledger/adapter/in/web/`

HTTP 요청을 유스케이스 명령으로 바꿔 넘깁니다. 비즈니스 규칙은 두지 않습니다.

| 파일 | API |
|---|---|
| [ExpenseController](src/main/java/com/example/ledger/adapter/in/web/ExpenseController.java) | `POST /api/expenses`: JSON 등록, 또는 이미지를 함께 보내는 multipart 등록(화면이 사용). 날짜가 없으면 오늘로 등록<br>`GET /api/expenses/images/{id}`: 이미지 원본 (하루 캐시, `X-Content-Type-Options: nosniff`)<br>`DELETE /api/expenses?ids=1,2,3`: 여러 건 삭제, `{"deleted": 지운 건수}`를 돌려줌 |
| [BudgetController](src/main/java/com/example/ledger/adapter/in/web/BudgetController.java) | `PUT /api/budgets/{yyyy-MM}`: 월 예산 설정 |
| [ReportController](src/main/java/com/example/ledger/adapter/in/web/ReportController.java) | `GET /api/reports/{yyyy-MM}`: 월 리포트 |
| [ApiExceptionHandler](src/main/java/com/example/ledger/adapter/in/web/ApiExceptionHandler.java) | 도메인 규칙 위반과 업로드 크기 초과를 `400 {"message": ...}`로 바꿈 |

---

## 6. adapter.out: 포트 구현

### persistence (JPA, H2)

`src/main/java/com/example/ledger/adapter/out/persistence/`

| 파일 | 역할 |
|---|---|
| [ExpenseJpaEntity](src/main/java/com/example/ledger/adapter/out/persistence/ExpenseJpaEntity.java), [ExpenseJpaRepository](src/main/java/com/example/ledger/adapter/out/persistence/ExpenseJpaRepository.java) | `expense` 테이블 (금액, 카테고리, 날짜, 메모). 삭제는 한 번의 `DELETE` 문으로 처리(`deleteByIdIn`) |
| [ExpensePersistenceAdapter](src/main/java/com/example/ledger/adapter/out/persistence/ExpensePersistenceAdapter.java) | `SaveExpensePort`, `LoadExpensesPort`, `DeleteExpensePort` 구현. 엔티티와 도메인 객체를 서로 변환 |
| [BudgetJpaEntity](src/main/java/com/example/ledger/adapter/out/persistence/BudgetJpaEntity.java), [BudgetJpaRepository](src/main/java/com/example/ledger/adapter/out/persistence/BudgetJpaRepository.java), [BudgetPersistenceAdapter](src/main/java/com/example/ledger/adapter/out/persistence/BudgetPersistenceAdapter.java) | 예산 테이블과 `BudgetPort` 구현 |
| [ExpenseImageJpaEntity](src/main/java/com/example/ledger/adapter/out/persistence/ExpenseImageJpaEntity.java) | `expense_image` 테이블. 이미지 내용은 BLOB으로 저장 |
| [ExpenseImageJpaRepository](src/main/java/com/example/ledger/adapter/out/persistence/ExpenseImageJpaRepository.java) | 목록용으로 이미지 내용 없이 id만 읽는 조회(`ImageRef`), 지출 id로 이미지를 한 번에 지우는 삭제(`deleteByExpenseIdIn`) |
| [ExpenseImagePersistenceAdapter](src/main/java/com/example/ledger/adapter/out/persistence/ExpenseImagePersistenceAdapter.java) | `ExpenseImagePort` 구현 |

JPA 엔티티는 도메인 객체와 따로 둡니다. 그래서 JPA가 도메인 안으로 들어오지 않고, DB를 바꿀 때는 이 패키지만 새로 만들면 됩니다.

### notification

`src/main/java/com/example/ledger/adapter/out/notification/`

| 파일 | 역할 |
|---|---|
| [LogBudgetAlertAdapter](src/main/java/com/example/ledger/adapter/out/notification/LogBudgetAlertAdapter.java) | `BudgetAlertPort` 구현. 예산 초과를 경고 로그로 남김 |

---

## 7. config: 조립

| 파일 | 역할 |
|---|---|
| [UseCaseConfig](src/main/java/com/example/ledger/config/UseCaseConfig.java) | 유스케이스 구현체 5개에 포트 구현체를 넣어 Spring 빈으로 만듦. 지출 삭제는 `TransactionTemplate`으로 감싸 하나의 트랜잭션으로 실행(서비스가 Spring을 모르므로 여기서 감쌈). 오늘 날짜 계산용 `Clock`도 빈으로 등록 |
| [LedgerApplication](src/main/java/com/example/ledger/LedgerApplication.java) | Spring Boot 시작점 |

---

## 8. resources

`src/main/resources/`

| 파일 | 내용 |
|---|---|
| [application.yml](src/main/resources/application.yml) | H2 **인메모리** DB(서버를 재시작하면 데이터가 사라짐), 테이블 자동 생성, H2 콘솔(`/h2-console`) 사용, 업로드 한도(파일당 6MB, 요청당 32MB. 정확한 규칙은 도메인이 검사) |
| [static/index.html](src/main/resources/static/index.html) | 화면 전체가 들어 있는 HTML 파일 하나. Spring Boot가 `/` 경로로 제공 |

### 화면 구성 (`index.html`)

| 영역 | 기능 |
|---|---|
| 상단 | 월 선택(이번 달까지만), 엑셀 다운로드 버튼(연속 클릭 방지) |
| 월 요약 | 예산, 지출 합계, 남은 예산과 진행 막대. 예산을 넘으면 빨간색 |
| 예산 설정 | 0 이하나 빈칸이면 저장 불가. 예산이 없던 달은 "저장", 있으면 "수정". 저장하면 입력칸을 비움 |
| 지출 등록 | 금액, 카테고리(식비·교통비·유흥·경조사·생활용품·기타), 메모, 이미지(최대 5장), 날짜(선택한 월 안에서만, 기본값 오늘). 각 칸 검증 |
| 카테고리별 합계 | 6개 카테고리를 항상 보여주고 비율과 금액 표시 |
| 지출 목록 | 카테고리 필터, 날짜·카테고리 제목 클릭 정렬(기본은 최근 날짜순), 메모, 이미지 썸네일(누르면 원본), 합계. 행 체크박스와 전체 선택, "선택 삭제 (N)" 버튼(삭제 전 확인 창, 이미지도 함께 삭제). 화면에 안 보이게 된 행은 선택에서 자동으로 빠짐 |
| 엑셀 | 시트 1은 카테고리별 합계, 시트 2는 지출 목록(메모 포함, 최근 날짜순). 테두리와 제목 줄 서식 적용. 브라우저에서 `xlsx-js-style`(CDN)로 생성 |
