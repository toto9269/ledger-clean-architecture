# 소스 구조 안내

가계부 API의 소스를 정리한 문서입니다. 먼저 계층(`controller`, `service`, `dto`, `entity`, `repository`)으로 나누고, 각 계층 안을 메뉴(`expense` 지출, `budget` 예산, `report` 월 리포트)로 나눴습니다.

## 전체 구조

```
com.example.ledger
├─ LedgerApplication
├─ controller
│   ├─ expense      ExpenseController
│   ├─ budget       BudgetController
│   └─ report       ReportController
├─ service
│   ├─ expense      ExpenseService
│   ├─ budget       BudgetService, BudgetAlertService
│   └─ report       ReportService
├─ dto
│   ├─ expense      AddExpenseRequest, UpdateExpenseRequest, ExpenseSaveResponse, DeleteExpensesResponse,
│   │               AddExpenseCommand, UpdateExpenseCommand, ExpenseSaveResult, ImageFile
│   ├─ budget       SetBudgetRequest
│   └─ report       MonthlyReportResponse, MonthlyReport
├─ entity
│   ├─ expense      Expense, ExpenseImage
│   └─ budget       Budget
├─ repository
│   ├─ expense      ExpenseRepository, ExpenseImageRepository
│   └─ budget       BudgetRepository
└─ common
    ├─ config       ClockConfig
    └─ exception    ApiExceptionHandler
```

`report`는 자기 테이블이 없어서 `entity`와 `repository`에는 없습니다.

**요청 흐름:** `controller` → `service` → `repository` → DB

- **controller:** HTTP 요청을 받아 요청 DTO(`...Request`)를 서비스용 DTO(`...Command`)로 바꿔 서비스를 부르고, 결과(`...Result`)를 응답 DTO(`...Response`)로 바꿔 돌려줍니다.
- **service:** 업무 흐름을 처리합니다. 클래스에 `@Transactional`이 붙어 있어서 메서드 하나가 하나의 트랜잭션입니다. 중간에 예외가 나면 그 메서드에서 한 저장과 삭제가 모두 되돌려집니다.
- **dto:** 계층 사이에 주고받는 데이터입니다. 이름 끝으로 용도를 구분합니다.

  | 끝 이름 | 쓰는 곳 |
  |---|---|
  | `Request` / `Response` | controller가 HTTP로 받고 돌려주는 값 |
  | `Command` / `Result` | controller와 service 사이에 주고받는 값 |

- **entity:** JPA 엔티티이자 규칙을 가진 객체입니다. 만들 때와 바꿀 때 규칙(금액 > 0 등)을 검사합니다.
- **repository:** Spring Data JPA 인터페이스입니다.

**의존 방향:** [ArchitectureTest](src/test/java/com/example/ledger/ArchitectureTest.java)가 순환 의존이 생기면 빌드를 실패시킵니다.
- 계층: `controller → service → repository → entity`, `dto`는 controller와 service가 함께 씀
- 메뉴: `report → expense, budget`, `expense → budget`

---

## 1. controller: HTTP 입구

| 메뉴 | 파일 | API |
|---|---|---|
| expense | [ExpenseController](src/main/java/com/example/ledger/controller/expense/ExpenseController.java) | `POST /api/expenses`: JSON 등록, 또는 이미지를 함께 보내는 multipart 등록(화면이 사용). 날짜가 없으면 오늘<br>`PUT /api/expenses/{id}`: 지출 변경 (금액과 날짜 필수, 이미지는 그대로)<br>`DELETE /api/expenses?ids=1,2,3`: 여러 건 삭제<br>`GET /api/expenses/images/{id}`: 이미지 원본 (하루 캐시, `X-Content-Type-Options: nosniff`) |
| budget | [BudgetController](src/main/java/com/example/ledger/controller/budget/BudgetController.java) | `PUT /api/budgets/{yyyy-MM}`: 월 예산 설정 |
| report | [ReportController](src/main/java/com/example/ledger/controller/report/ReportController.java) | `GET /api/reports/{yyyy-MM}`: 월 리포트 |

---

## 2. service: 업무 흐름

| 메뉴 | 파일 | 흐름 |
|---|---|---|
| expense | [ExpenseService](src/main/java/com/example/ledger/service/expense/ExpenseService.java) | **등록:** 지출과 이미지를 먼저 모두 검사 → 지출 저장 → 이미지 저장 → 월 합계 계산 → 예산을 넘었으면 알림<br>**변경:** 지출을 찾아(없으면 예외) 규칙 재검사 후 변경 → 월 합계 계산 → 예산 초과 알림<br>**삭제:** 빈 목록 거부 → 딸린 이미지 먼저 삭제(고아 행 방지) → 지출 삭제 → 지운 건수가 요청과 다르면 예외(트랜잭션이 전부 되돌림)<br>**이미지 조회:** 이미지 한 장을 읽어 돌려줌 |
| budget | [BudgetService](src/main/java/com/example/ledger/service/budget/BudgetService.java) | 그 달 예산을 새로 정하거나 바꿈 |
| budget | [BudgetAlertService](src/main/java/com/example/ledger/service/budget/BudgetAlertService.java) | 예산 초과 알림. 지금은 경고 로그로 남김 |
| report | [ReportService](src/main/java/com/example/ledger/service/report/ReportService.java) | 한 달 지출을 읽어 합계와 카테고리별 합계를 계산하고, 예산과 지출별 이미지 id를 붙임 (읽기 전용 트랜잭션). 화면의 월 요약, 카테고리별 합계, 지출 목록, 엑셀이 모두 이 리포트 하나로 채워짐 |

---

## 3. dto: 주고받는 데이터

| 메뉴 | 파일 | 용도 | 내용 |
|---|---|---|---|
| expense | [AddExpenseRequest](src/main/java/com/example/ledger/dto/expense/AddExpenseRequest.java) | 요청 | JSON 등록 (금액, 카테고리, 날짜, 메모) |
| expense | [UpdateExpenseRequest](src/main/java/com/example/ledger/dto/expense/UpdateExpenseRequest.java) | 요청 | 변경 (금액, 카테고리, 날짜, 메모) |
| expense | [ExpenseSaveResponse](src/main/java/com/example/ledger/dto/expense/ExpenseSaveResponse.java) | 응답 | 등록·변경 결과 (지출 id, 월 합계, 예산 초과 여부) |
| expense | [DeleteExpensesResponse](src/main/java/com/example/ledger/dto/expense/DeleteExpensesResponse.java) | 응답 | 삭제 결과 (`{"deleted": 지운 건수}`) |
| expense | [AddExpenseCommand](src/main/java/com/example/ledger/dto/expense/AddExpenseCommand.java) | 서비스 입력 | 등록에 필요한 값 (금액, 카테고리, 날짜, 메모, 이미지 목록) |
| expense | [UpdateExpenseCommand](src/main/java/com/example/ledger/dto/expense/UpdateExpenseCommand.java) | 서비스 입력 | 변경에 필요한 값 (지출 id, 금액, 카테고리, 날짜, 메모) |
| expense | [ExpenseSaveResult](src/main/java/com/example/ledger/dto/expense/ExpenseSaveResult.java) | 서비스 결과 | 등록·변경 결과 (지출 id, 그 달 합계, 예산 초과 여부) |
| expense | [ImageFile](src/main/java/com/example/ledger/dto/expense/ImageFile.java) | 서비스 입력·결과 | 이미지 한 장의 형식과 내용 (올릴 때와 내려받을 때 모두 사용) |
| budget | [SetBudgetRequest](src/main/java/com/example/ledger/dto/budget/SetBudgetRequest.java) | 요청 | 예산 설정 (`{"limit": 500000}`) |
| report | [MonthlyReportResponse](src/main/java/com/example/ledger/dto/report/MonthlyReportResponse.java) | 응답 | 월 리포트 (월, 합계, 예산, 남은 예산, 카테고리별 합계, 지출 목록) |
| report | [MonthlyReport](src/main/java/com/example/ledger/dto/report/MonthlyReport.java) | 서비스 결과 | 서비스가 만든 리포트 (지출 항목 `Item` 포함) |

---

## 4. entity: 테이블과 규칙

| 메뉴 | 파일 | 테이블 | 지키는 규칙 |
|---|---|---|---|
| expense | [Expense](src/main/java/com/example/ledger/entity/expense/Expense.java) | `expense` (금액, 카테고리, 날짜, 메모) | 금액은 0보다 큼, 카테고리와 날짜는 필수, 메모는 100자 이하이고 비어 있으면 null. 규칙을 어기는 변경(`change`)은 아무것도 바꾸지 않음 |
| expense | [ExpenseImage](src/main/java/com/example/ledger/entity/expense/ExpenseImage.java) | `expense_image` (지출 id, 형식, 이미지 BLOB) | JPG·PNG·GIF·WEBP만, 장당 5MB 이하, 빈 파일 거부, 지출당 5장까지(`checkCount`) |
| budget | [Budget](src/main/java/com/example/ledger/entity/budget/Budget.java) | `budget` (기본 키는 "2026-10" 같은 연월 문자열, 예산 금액) | 예산은 0보다 큼. 초과 여부(`isExceededBy`)와 남은 금액(`remaining`) 계산 |

---

## 5. repository: DB 조회와 저장

| 메뉴 | 파일 | 주요 메서드 |
|---|---|---|
| expense | [ExpenseRepository](src/main/java/com/example/ledger/repository/expense/ExpenseRepository.java) | 기간 안 지출 조회, 기간 안 금액 합계(`sumAmountBetween`), 한 번의 `DELETE` 문으로 여러 건 삭제(`deleteByIdIn`) |
| expense | [ExpenseImageRepository](src/main/java/com/example/ledger/repository/expense/ExpenseImageRepository.java) | 목록용으로 이미지 내용 없이 id만 읽는 조회(`ImageRef`), 지출 id로 이미지 일괄 삭제(`deleteByExpenseIdIn`) |
| budget | [BudgetRepository](src/main/java/com/example/ledger/repository/budget/BudgetRepository.java) | 연월로 예산 찾기(`findByMonth`) |

---

## 6. common: 공통

| 폴더 | 파일 | 역할 |
|---|---|---|
| config | [ClockConfig](src/main/java/com/example/ledger/common/config/ClockConfig.java) | 오늘 날짜 계산에 쓰는 `Clock` 빈 |
| exception | [ApiExceptionHandler](src/main/java/com/example/ledger/common/exception/ApiExceptionHandler.java) | 엔티티 규칙 위반과 업로드 크기 초과를 `400 {"message": ...}`로 바꿈 |
| (루트) | [LedgerApplication](src/main/java/com/example/ledger/LedgerApplication.java) | Spring Boot 시작점 |

---

## 7. resources

`src/main/resources/`

| 파일 | 내용 |
|---|---|
| [application.yml](src/main/resources/application.yml) | H2 **인메모리** DB(서버를 재시작하면 데이터가 사라짐), 테이블 자동 생성, H2 콘솔(`/h2-console`) 사용, 업로드 한도(파일당 6MB, 요청당 32MB. 정확한 규칙은 엔티티가 검사) |
| [static/index.html](src/main/resources/static/index.html) | 화면 전체가 들어 있는 HTML 파일 하나. Spring Boot가 `/` 경로로 제공 |

### 화면 구성 (`index.html`)

| 영역 | 기능 |
|---|---|
| 상단 | 월 선택(이번 달까지만), 엑셀 다운로드 버튼(연속 클릭 방지) |
| 월 요약 | 예산, 지출 합계, 남은 예산과 진행 막대. 예산을 넘으면 빨간색 |
| 예산 설정 | 0 이하나 빈칸이면 저장 불가. 예산이 없던 달은 "저장", 있으면 "수정". 저장하면 입력칸을 비움 |
| 지출 등록 | 금액, 카테고리(식비·교통비·유흥·경조사·생활용품·기타), 메모, 이미지(최대 5장), 날짜(선택한 월 안에서만, 기본값 오늘). 각 칸 검증 |
| 카테고리별 합계 | 6개 카테고리를 항상 보여주고 비율과 금액 표시. 제목 오른쪽에 그 달의 지출 합계 표시 |
| 지출 목록 | 카테고리 필터, 날짜·카테고리 제목 클릭 정렬(기본은 최근 날짜순), 메모, 이미지 썸네일(누르면 원본), 합계. 행 체크박스와 전체 선택, "선택 삭제 (N)" 버튼(삭제 전 확인 창, 이미지도 함께 삭제). 화면에 안 보이게 된 행은 선택에서 자동으로 빠짐. 행을 누르면 지출 변경 팝업이 열림 |
| 지출 변경 팝업 | 레이어 팝업(모달). 금액, 카테고리, 메모, 날짜(선택한 월 안)를 고치고 "변경"을 누르면 검사 후 저장하고 닫힘. 화면 전체를 다시 그림. 이미지는 보기만 가능. 오류는 팝업 안에 표시. 취소, ESC, 바깥 클릭으로 저장 없이 닫힘 |
| 엑셀 | 시트 1은 카테고리별 합계, 시트 2는 지출 목록(메모 포함, 최근 날짜순). 테두리와 제목 줄 서식 적용. 브라우저에서 `xlsx-js-style`(CDN)로 생성 |

---

## 8. 테스트

`src/test/java/com/example/ledger/` (소스와 같은 계층 > 메뉴 폴더)

| 파일 | 검사하는 것 |
|---|---|
| [LedgerApiTest](src/test/java/com/example/ledger/LedgerApiTest.java) | 실제 웹, 서비스, JPA(H2)를 연결해 API 전체 동작 확인 (등록, 변경, 삭제와 롤백, 이미지, 400 응답) |
| [ExpenseEntityTest](src/test/java/com/example/ledger/entity/expense/ExpenseEntityTest.java), [BudgetTest](src/test/java/com/example/ledger/entity/budget/BudgetTest.java) | 엔티티 규칙 (DB 없이) |
| [ExpenseServiceTest](src/test/java/com/example/ledger/service/expense/ExpenseServiceTest.java) | 지출 서비스 (예산 초과 알림 로그, 이미지 저장, 잘못된 이미지면 지출도 저장 안 함, 변경, 삭제 롤백) |
| [ReportServiceTest](src/test/java/com/example/ledger/service/report/ReportServiceTest.java) | 월 합계와 카테고리별 합계 계산, 다른 달을 섞지 않음 |
| [ArchitectureTest](src/test/java/com/example/ledger/ArchitectureTest.java) | 계층 사이, 메뉴 사이 순환 의존 금지 |
