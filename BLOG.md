# DB를 바꾸려다 포기했던 가계부, 클린 아키텍처로 다시 만들기

> 클린 아키텍처, 컴포넌트 원칙, SOLID를 공부하고 나서 예전에 만든 가계부 백엔드를 처음부터 다시 설계해 봤습니다. 왜 바꿨는지, 어떻게 나눴는지, 직접 해 보니 무엇이 좋고 무엇이 번거로웠는지 정리합니다.

- 사용 기술: Java 17, Spring Boot 3.3, Spring Data JPA, H2, JUnit 5, ArchUnit
- 소스 코드: (GitHub 저장소 링크)

---

## 1. 프로젝트 소개

### 목표
**"핵심 규칙은 그대로 두고, DB나 알림 방식은 언제든 갈아 끼울 수 있는 가계부"**를 만드는 것입니다.
기능을 많이 넣기보다는 기능 몇 개를 제대로 된 구조로 만드는 데 집중했습니다.

### 주요 기능
| 기능 | API | 설명 |
|---|---|---|
| 월 예산 설정 | `PUT /api/budgets/{yyyy-MM}` | 달마다 쓸 수 있는 금액을 정합니다 |
| 지출 등록 | `POST /api/expenses` | 금액, 카테고리, 날짜로 지출을 기록합니다 |
| 예산 초과 알림 | (지출 등록 시 자동) | 그 달 합계가 예산을 넘으면 알림을 보냅니다 |
| 월별 리포트 | `GET /api/reports/{yyyy-MM}` | 합계, 남은 예산, 카테고리별 합계, 지출 목록을 보여줍니다 |

```json
// POST /api/expenses → 예산 50,000원인 달에 누적 55,000원이 된 경우
{ "expenseId": 2, "monthlyTotal": 55000, "budgetExceeded": true }
```

---

## 2. 클린 아키텍처를 선택한 이유

### 예전 구조의 문제점
처음 가계부를 만들 때는 기능이 돌아가는 것만 신경 썼습니다. 흔히 보는 `controller / service / repository / entity` 구조였고, 서비스는 대략 이런 모습이었습니다.

```java
@Service
public class ExpenseService {
    @Autowired private ExpenseRepository expenseRepository;   // JPA 에 직접 의존
    @Autowired private BudgetService budgetService;            // 서로 부르는 순환 의존
    @Autowired private JavaMailSender mailSender;              // 메일 라이브러리에 직접 의존

    public ExpenseResponse add(ExpenseRequest request) {       // 웹 DTO 를 그대로 받음
        if (request.getAmount() <= 0) throw new IllegalArgumentException("금액 오류");
        Expense expense = new Expense();                       // @Entity — 규칙 없이 getter/setter 만
        expense.setAmount(request.getAmount());
        expenseRepository.save(expense);

        long total = expenseRepository.sumByMonth(YearMonth.now());
        if (total > budgetService.getBudget()) {
            mailSender.send(/* 예산 초과 메일 */);
        }
        return new ExpenseResponse(expense.getId());
    }
}
```

이 구조로 지내면서 실제로 겪은 문제는 세 가지였습니다.

1. **DB에 묶여 있었습니다.**
   DB를 바꿔 보려고 했는데, `@Entity`가 붙은 클래스가 컨트롤러부터 서비스까지 다 퍼져 있어서 고칠 곳이 너무 많았습니다. 결국 포기했습니다.
   "예산을 넘었는지 판단한다"는 가계부의 **핵심 규칙**이 JPA라는 **세부 기술**과 한 몸이 되어 있었던 겁니다.
2. **프레임워크 없이는 아무것도 테스트할 수 없었습니다.**
   "예산을 1원이라도 넘으면 알린다"는 규칙 하나를 확인하려고 해도 DB와 메일 서버가 필요했습니다. 그래서 테스트를 거의 안 짰고, 고칠 때마다 직접 눌러 봤습니다.
3. **바뀌는 이유가 한 곳에 몰려 있었습니다.**
   알림 문구, 예산 계산, 저장 방식 중 무엇을 바꾸든 `ExpenseService`를 열어야 했습니다. 거기다 `ExpenseService`와 `BudgetService`가 서로를 부르고 있어서, 하나를 고치면 다른 하나가 깨질까 봐 불안했습니다.

### 그래서 클린 아키텍처
강의에서 가장 와닿았던 말은 **"설계의 목적은 시스템을 만들고 유지보수하는 비용을 줄이는 것"**이었습니다. 제 가계부는 기능은 돌아갔지만, 바꾸는 비용이 너무 비쌌습니다.

클린 아키텍처의 **의존성 규칙**, 즉 "소스 코드 의존성은 항상 안쪽(핵심 규칙)을 향해야 한다"를 적용하면 세 문제가 모두 풀릴 거라고 봤습니다.
- 핵심 규칙이 DB를 모르게 되면 DB를 바꿔도 핵심 규칙은 그대로입니다.
- 핵심 규칙이 프레임워크를 모르게 되면 순수 Java로 테스트할 수 있습니다.
- 바뀌는 이유별로 계층을 나누면 한 가지 변경이 한 곳에서 끝납니다.

---

## 3. 설계

### 계층과 의존 방향

```
        ┌──────────────────────────────────────────────┐
        │ adapter (Spring, JPA, 로그)                    │
        │   in.web  ─┐                     ┌─ out.persistence / out.notification
        │            ▼                     │ (포트 구현)
        │   ┌────────────────────────────────────┐     │
        │   │ application                        │◀────┘
        │   │   port.in  ← 유스케이스 약속        │
        │   │   service  ← 유스케이스 구현        │
        │   │   port.out ← 바깥에 요구하는 약속   │
        │   │   ┌──────────────────────────┐     │
        │   │   │ domain                   │     │
        │   │   │ Expense, Budget,         │     │
        │   │   │ MonthlyExpenses          │     │
        │   │   └──────────────────────────┘     │
        │   └────────────────────────────────────┘
        └──────────────────────────────────────────────┘
          화살표(의존)는 전부 바깥 → 안쪽
```

```
com.example.ledger
 ├─ domain                 Expense, Budget, MonthlyExpenses       ← 순수 Java
 ├─ application
 │   ├─ port.in            AddExpenseUseCase, SetBudgetUseCase, GetMonthlyReportUseCase
 │   ├─ port.out           SaveExpensePort, LoadExpensesPort, BudgetPort, BudgetAlertPort
 │   └─ service            유스케이스 구현 (Spring 어노테이션 없음)
 ├─ adapter
 │   ├─ in.web             컨트롤러
 │   └─ out
 │       ├─ persistence    JPA 엔티티·리포지토리·포트 구현
 │       └─ notification   예산 초과 알림 (지금은 로그)
 └─ config                 유스케이스 빈 조립
```

### ① Domain — 가계부의 규칙만 담는다
도메인에는 `import org.springframework`도, `@Entity`도 없습니다. "지출 금액은 0보다 커야 한다", "합계가 예산보다 크면 초과다" 같은 규칙을 객체 스스로 지킵니다.

```java
public class Budget {
    private final YearMonth month;
    private final long limit;

    public Budget(YearMonth month, long limit) {
        if (limit < 0) throw new IllegalArgumentException("예산은 0 이상이어야 합니다");
        this.month = Objects.requireNonNull(month);
        this.limit = limit;
    }

    public boolean isExceededBy(long monthlyTotal) { return monthlyTotal > limit; }
    public long remaining(long monthlyTotal)       { return limit - monthlyTotal; }
}
```

### ② Application — 유스케이스와 포트
유스케이스는 **포트(인터페이스)**만 압니다. 들어오는 포트(`port.in`)는 컨트롤러가 호출하는 약속이고, 나가는 포트(`port.out`)는 유스케이스가 바깥에 "이런 기능을 달라"고 요구하는 약속입니다.

```java
public interface SaveExpensePort  { Expense save(Expense expense); }
public interface LoadExpensesPort { MonthlyExpenses loadMonthly(YearMonth month); }
public interface BudgetPort       { void save(Budget budget); Optional<Budget> load(YearMonth month); }
public interface BudgetAlertPort  { void alertExceeded(Budget budget, long monthlyTotal); }
```

```java
public class AddExpenseService implements AddExpenseUseCase {   // @Service 없음
    // 생성자로 포트 4개를 받는다 (생략)

    @Override
    public Result add(Command command) {
        Expense saved = saveExpensePort.save(
                Expense.create(command.amount(), command.category(), command.spentAt()));

        YearMonth month = YearMonth.from(saved.getSpentAt());
        long monthlyTotal = loadExpensesPort.loadMonthly(month).total();

        Optional<Budget> budget = budgetPort.load(month);
        boolean exceeded = budget.map(b -> b.isExceededBy(monthlyTotal)).orElse(false);
        if (exceeded) {
            budgetAlertPort.alertExceeded(budget.get(), monthlyTotal);
        }
        return new Result(saved.getId(), monthlyTotal, exceeded);
    }
}
```
여기에는 JPA도 메일도 HTTP도 없습니다. 읽어 보면 **가계부가 하는 일**만 남아 있습니다.

### ③ Adapter — 세부 기술은 바깥에만
JPA는 `adapter.out.persistence` 안에만 있습니다. DB 테이블 모양(`ExpenseJpaEntity`)과 도메인 객체(`Expense`)를 일부러 분리하고, 어댑터가 둘 사이를 변환합니다.

```java
@Component
class ExpensePersistenceAdapter implements SaveExpensePort, LoadExpensesPort {
    private final ExpenseJpaRepository repository;

    @Override
    public Expense save(Expense expense) {
        ExpenseJpaEntity saved = repository.save(
                new ExpenseJpaEntity(expense.getAmount(), expense.getCategory(), expense.getSpentAt()));
        return Expense.of(saved.getId(), saved.getAmount(), saved.getCategory(), saved.getSpentAt());
    }
    // loadMonthly 생략
}
```

알림도 같습니다. 지금은 로그로 남기지만, 메일이나 카카오 알림톡으로 바꾸려면 `BudgetAlertPort` 구현체를 하나 새로 만들면 됩니다. 유스케이스는 고칠 필요가 없습니다.

```java
@Component
class LogBudgetAlertAdapter implements BudgetAlertPort {
    @Override
    public void alertExceeded(Budget budget, long monthlyTotal) {
        log.warn("[예산 초과] {} 예산 {}원, 현재 지출 {}원", budget.getMonth(), budget.getLimit(), monthlyTotal);
    }
}
```

### ④ Config — 조립은 한 곳에서
유스케이스에 `@Service`를 붙이지 않았기 때문에, Spring 빈 등록은 설정 클래스 한 곳에서 합니다. 프레임워크와 핵심 코드가 만나는 지점이 여기 하나뿐입니다.

```java
@Configuration
public class UseCaseConfig {
    @Bean
    AddExpenseUseCase addExpenseUseCase(SaveExpensePort save, LoadExpensesPort load,
                                        BudgetPort budget, BudgetAlertPort alert) {
        return new AddExpenseService(save, load, budget, alert);
    }
}
```

---

## 4. 적용한 원칙 정리

| 원칙 | 이 프로젝트에서 |
|---|---|
| **의존성 규칙** | `domain`, `application`에는 Spring/JPA import가 하나도 없습니다 |
| **SRP** | 저장, 알림, 웹 변환, 규칙이 각각 다른 클래스에 있어서 바뀌는 이유가 하나씩입니다 |
| **OCP** | 알림 방식이나 DB를 바꿀 때 새 어댑터를 추가하고, 유스케이스는 고치지 않습니다 |
| **ISP** | 포트를 쓰임새별로 작게 나눴습니다. 리포트 조회는 `LoadExpensesPort`와 `BudgetPort`만 압니다 |
| **DIP** | 유스케이스(고수준)와 JPA 어댑터(저수준)가 모두 포트(추상)에 의존합니다 |
| **ADP** | `adapter → application → domain` 한 방향이라 패키지 순환이 없습니다 |
| **SDP / SAP** | 자주 바뀌는 어댑터가 안정된 포트를 의존하고, 안정된 쪽(포트)은 인터페이스입니다 |
| **CCP** | 지출 저장 방식이 바뀌면 `adapter.out.persistence` 안에서 끝납니다 |

---

## 5. 구조를 테스트로 지키기

### 핵심 규칙은 DB 없이 테스트
포트를 메모리 가짜로 바꿔 끼우면 Spring을 띄우지 않고 유스케이스를 바로 검증할 수 있습니다. 예전에는 불가능했던 테스트입니다.

```java
@Test
void 예산을_넘는_순간_알린다() {
    ledger.save(new Budget(OCTOBER, 50_000));   // 메모리 가짜 저장소
    addExpense.add(command(30_000));

    AddExpenseUseCase.Result result = addExpense.add(command(25_000));

    assertThat(result.budgetExceeded()).isTrue();
    assertThat(alerts).containsExactly("2026-10:55000");
}
```

### 의존성 규칙도 테스트로
구조는 시간이 지나면 무너지기 쉽습니다. "급하니까 서비스에서 JPA 리포지토리를 바로 부르자" 같은 일이 생기니까요. 그래서 **ArchUnit**으로 규칙을 코드로 만들었습니다. 누군가 도메인에 `@Entity`를 붙이면 빌드가 실패합니다.

```java
@Test
void 유스케이스는_어댑터와_프레임워크를_모른다() {
    noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..adapter..", "..config..", "org.springframework..", "jakarta.persistence..")
            .check(classes);
}

@Test
void 패키지_사이에_순환_의존이_없다() {
    slices().matching("com.example.ledger.(**)").should().beFreeOfCycles().check(classes);
}
```

### 테스트 결과
| 테스트 | 개수 | 내용 |
|---|---|---|
| `DomainTest` | 5 | 금액·카테고리 검증, 예산 초과, 월 합계·카테고리별 합계 |
| `AddExpenseServiceTest` | 3 | 메모리 가짜 포트로 예산 초과 알림 검증 |
| `ArchitectureTest` | 3 | 의존성 규칙, 순환 의존 금지 |
| `LedgerApiTest` | 2 | 웹 → 유스케이스 → JPA(H2) 통합 |

`./gradlew test` 결과 13개 모두 통과했습니다.

---

## 6. 해 보고 느낀 점

### 좋았던 점
- **"DB를 바꾸면 어디를 고치나?"에 바로 답할 수 있게 됐습니다.** `adapter.out.persistence`만 고치면 됩니다. 예전에 DB를 바꾸다 포기했던 이유가 구조 때문이었다는 걸 확실히 알게 됐습니다.
- **테스트가 쉬워지니 테스트를 짜게 됐습니다.** 핵심 규칙 테스트는 Spring 없이 돌아서, 도메인·유스케이스 테스트 8개가 합쳐서 0.1초도 걸리지 않습니다.
- **코드를 읽으면 기능이 보입니다.** `AddExpenseService`만 읽어도 "지출을 저장하고, 월 합계를 구하고, 예산을 넘으면 알린다"는 흐름이 그대로 보입니다.

### 번거로웠던 점
- **클래스가 많아졌습니다.** 지출 하나에 도메인 객체, JPA 엔티티, 포트, 어댑터가 따로 있고, 그 사이 변환 코드도 써야 합니다. 작은 프로젝트에서는 과하다고 느낄 수 있습니다.
- **트랜잭션 위치를 고민해야 했습니다.** 유스케이스가 Spring을 모르기 때문에 `@Transactional`을 바로 붙일 수 없습니다. 이번에는 단순하게 두었고, 다음에는 설정 쪽에서 프록시로 감싸는 방법을 적용해 볼 생각입니다.

### 결론
모든 기능을 이렇게 나눌 필요는 없다고 생각합니다. 단순 조회처럼 규칙이 거의 없는 기능은 계층을 줄여도 됩니다. 하지만 **예산 계산처럼 규칙이 있고 자주 바뀌는 부분**은 처음부터 핵심 규칙과 세부 기술을 떼어 놓는 게 결국 비용을 줄인다는 걸 이번 프로젝트로 직접 확인했습니다.
"일단 돌아가는 코드"에서 **"바꾸기 쉬운 코드"**로 생각을 바꾸는 계기가 됐습니다.
