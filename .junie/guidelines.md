Here’s a project-specific **GUIDELINES.md** you can drop into your repo, with DTOs, mappers, and Flyway-based seeding baked in.

````markdown
# Product Feedback App – Project Guidelines

These guidelines define how to structure and implement the **Product Feedback App** based on the Frontend Mentor challenge, using:

- Spring Boot (Web, Thymeleaf, Data JPA, Security, Validation)
- MySQL
- Thymeleaf + CSS
- DTOs + Mapper layer
- Flyway for schema and data seeding

---

## 1. Architecture & Layering

Use a classic layered architecture with clear separation of concerns:

- **Web layer** (`controller`)
  - Handles HTTP requests/responses.
  - Only deals with **DTOs**, never with JPA entities directly.
  - Uses services for business logic.
- **Service layer** (`service`)
  - Implements business use cases.
  - Works with **entities** internally.
  - Uses **mappers** to convert between entities and DTOs.
  - Encapsulates transactional boundaries.
- **Repository layer** (`repository`)
  - Spring Data JPA interfaces.
  - No business logic.
- **Domain layer** (`domain` or `model`)
  - JPA entities (User, Feedback, Comment, Reply, Category, Upvote).
- **Mapper layer** (`mapper`)
  - Converts between entities and DTOs (request/response).
- **Config & Infrastructure** (`config`, `security`, `flyway`, etc.)
  - Spring Security configuration.
  - Flyway migrations.
  - Other infrastructure concerns (exception handling, file upload, etc.)

Controllers → Services → Repositories  
Controllers ↔ DTOs ↔ Mappers ↔ Entities

---

## 2. Package Structure

Recommended base package: `com.example.productfeedback`

Example structure:

```text
com.example.productfeedback
 ├─ config/
 │   ├─ WebConfig.java
 │   ├─ SecurityConfig.java
 │   └─ FlywayConfig.java (if needed)
 ├─ domain/
 │   ├─ User.java
 │   ├─ Category.java
 │   ├─ Feedback.java
 │   ├─ Comment.java
 │   ├─ Reply.java
 │   └─ Upvote.java
 ├─ dto/
 │   ├─ feedback/
 │   │   ├─ FeedbackListItemDto.java
 │   │   ├─ FeedbackDetailDto.java
 │   │   ├─ FeedbackCreateRequest.java
 │   │   └─ FeedbackUpdateRequest.java
 │   ├─ comment/
 │   │   ├─ CommentDto.java
 │   │   ├─ CommentCreateRequest.java
 │   │   └─ CommentUpdateRequest.java
 │   ├─ reply/
 │   │   ├─ ReplyDto.java
 │   │   ├─ ReplyCreateRequest.java
 │   │   └─ ReplyUpdateRequest.java
 │   ├─ user/
 │   │   ├─ UserProfileDto.java
 │   │   └─ UserProfileUpdateRequest.java
 │   └─ auth/
 │       ├─ RegisterRequest.java
 │       └─ LoginRequest.java (if needed)
 ├─ mapper/
 │   ├─ FeedbackMapper.java
 │   ├─ CommentMapper.java
 │   ├─ ReplyMapper.java
 │   └─ UserMapper.java
 ├─ repository/
 │   ├─ UserRepository.java
 │   ├─ FeedbackRepository.java
 │   ├─ CommentRepository.java
 │   ├─ ReplyRepository.java
 │   ├─ CategoryRepository.java
 │   └─ UpvoteRepository.java
 ├─ service/
 │   ├─ FeedbackService.java
 │   ├─ CommentService.java
 │   ├─ ReplyService.java
 │   ├─ UserService.java
 │   ├─ AuthService.java
 │   └─ UpvoteService.java
 ├─ web/
 │   ├─ controller/
 │   │   ├─ FeedbackController.java
 │   │   ├─ CommentController.java
 │   │   ├─ ReplyController.java
 │   │   ├─ AuthController.java
 │   │   ├─ ProfileController.java
 │   │   └─ AdminController.java
 │   └─ advice/
 │       └─ GlobalExceptionHandler.java
 └─ ProductFeedbackApplication.java
````

Templates go in `src/main/resources/templates`.
Static assets go in `src/main/resources/static`.

---

## 3. DTO & Mapping Guidelines

### 3.1 General Rules

* **Do NOT expose entities in controllers.**
  Controllers should only use DTOs for request and response bodies/models.
* Use separate DTOs for:

  * **List views** vs **detail views** (e.g., `FeedbackListItemDto` vs `FeedbackDetailDto`).
  * **Create/Update requests** and **read responses**.
* DTOs are part of the web contract: changes must be intentional and controlled.

### 3.2 DTO Types

Example for Feedback:

* `FeedbackListItemDto`

  * Minimal fields for listing page (id, title, categoryName, status, upvoteCount, commentCount).
* `FeedbackDetailDto`

  * Includes full description, author info, comments & replies.
* `FeedbackCreateRequest`

  * Fields incoming from “Create Feedback” form (title, description, categoryId).
* `FeedbackUpdateRequest`

  * Same as create plus `status` if editable.

For comments and replies, follow the same pattern:

* `[Comment|Reply]Dto` – for rendering.
* `[Comment|Reply]CreateRequest` – for forms.
* `[Comment|Reply]UpdateRequest` – for edit forms.

### 3.3 Mapper Layer

* Use a dedicated mapper layer to convert between Entities and DTOs.
* You can use **MapStruct** or manual mapping. Recommended: **MapStruct** for consistency and less boilerplate.
* Mappers live in `com.example.productfeedback.mapper`.

Example (MapStruct style):

```java
@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    FeedbackListItemDto toListItemDto(Feedback entity);

    FeedbackDetailDto toDetailDto(Feedback entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "SUGGESTION")
    @Mapping(target = "author", ignore = true) // set in service
    @Mapping(target = "category", ignore = true) // set in service
    Feedback fromCreateRequest(FeedbackCreateRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "status", source = "status")
    void updateEntityFromRequest(FeedbackUpdateRequest dto, @MappingTarget Feedback entity);
}
```

**Rules:**

* Set security-sensitive fields (author, roles) in service layer, not mapper.
* For nested DTOs (e.g., feedback detail containing comments & replies), either:

  * Use MapStruct with nested mappings, **or**
  * Build nested structures in the service using composition of mapper calls.
* Mappers should not call repositories or services.

---

## 4. Flyway & Database Seeding Guidelines

### 4.1 Flyway Usage

* Use Flyway for **all schema changes and initial data seeding**.
* No `ddl-auto` in production; rely on Flyway.
* File location: `src/main/resources/db/migration`.

Naming convention:

* `V1__init_schema.sql`
* `V2__seed_reference_data.sql` (categories, maybe default admin)
* `V3__seed_initial_feedback_from_data_json.sql` **or** Java-based migration
* etc.

### 4.2 Schema Migration (V1)

`V1__init_schema.sql` should:

* Create tables for:

  * `users`
  * `categories`
  * `feedback`
  * `comments`
  * `replies`
  * `upvotes`
* Set up:

  * Primary keys.
  * Foreign keys.
  * Composite PK for `upvotes (user_id, feedback_id)`.
  * Indexes for common queries (e.g., feedback status, category, author).

### 4.3 Reference Data Seeding (V2)

`V2__seed_reference_data.sql` should:

* Insert default categories from the Frontend Mentor challenge (e.g., `Feature`, `UI`, `UX`, `Enhancement`, `Bug`).
* Insert at least one admin user (password hashed offline) or create admin later via SQL.
* Optionally insert known statuses as ENUM or check constraints depending on DB strategy.

### 4.4 Seeding from data.json (V3)

You want to use Flyway to seed the DB with the challenge’s `data.json`.

Two options (pick one and stay consistent):

#### Option A – Manual SQL seed

* Convert `data.json` into SQL insert statements:

  * Insert users for seed data (if needed).
  * Insert feedback records.
  * Insert comments and replies referencing these feedback items.
* Place this in `V3__seed_initial_feedback_from_json.sql`.

Pros: Simple runtime; no Java logic.
Cons: Needs manual conversion.

#### Option B – Java-based Flyway migration

* Create a Java-based migration class, e.g.:

  ```java
  public class V3__SeedInitialFeedbackFromJson extends BaseJavaMigration {
      @Override
      public void migrate(Context context) throws Exception {
          // Use Jackson to read data.json
          // Use JDBC from context.getConnection() to insert records
      }
  }
  ```

* Place `data.json` under `src/main/resources/data/`.

* Read JSON, then execute prepared statements.

Pros: Keeps `data.json` as source of truth.
Cons: Slightly more complex to implement.

**Guidelines:**

* Whichever option you choose, do **not** seed via `ApplicationRunner` for initial data. Keep it Flyway-driven.
* Seed only when needed. Flyway guarantees each migration runs once.

---

## 5. Controller & Service Guidelines

### 5.1 Controllers

* Location: `web.controller`.
* Responsibilities:

  * Map URLs to methods.
  * Accept/request DTOs.
  * Populate model attributes for Thymeleaf.
  * Call appropriate services.
* Must **not**:

  * Perform business logic.
  * Access repositories directly.
  * Work with entities directly.

Example pattern:

```java
@GetMapping("/feedback")
public String listFeedback(
        @RequestParam Optional<String> sort,
        @RequestParam Optional<String> category,
        Model model) {

    List<FeedbackListItemDto> feedback = feedbackService.getSuggestions(sort, category);
    model.addAttribute("feedback", feedback);
    return "feedback/list";
}
```

### 5.2 Services

* Location: `service`.
* Responsibilities:

  * Implement business use cases (create feedback, upvote, comment, etc.).
  * Enforce ownership rules (user can only edit/delete own content, except admin).
  * Handle transactional boundaries (`@Transactional`).
  * Use repositories and mappers.
* Must **not**:

  * Deal with HTTP specifics (no `Model`, no `HttpServletRequest`).
  * Return entities to controllers; map to DTOs.

Example pattern:

```java
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackMapper feedbackMapper;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<FeedbackListItemDto> getSuggestions(Optional<String> sort, Optional<String> categorySlug) {
        // query repository
        // map entities to DTOs via mapper
    }

    @Transactional
    public void createFeedback(FeedbackCreateRequest request) {
        Feedback entity = feedbackMapper.fromCreateRequest(request);
        User author = userService.getCurrentUser();
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(...);

        entity.setAuthor(author);
        entity.setCategory(category);

        feedbackRepository.save(entity);
    }
}
```

---

## 6. Validation & Error Handling

### 6.1 DTO Validation

* Add Bean Validation annotations on **request DTOs**:

  * `@NotBlank`, `@Size`, etc.
* Controllers should use `@Valid` and `BindingResult` to display error messages.

Example:

```java
@PostMapping("/feedback")
public String createFeedback(
        @Valid @ModelAttribute("feedback") FeedbackCreateRequest request,
        BindingResult bindingResult,
        Model model) {
    if (bindingResult.hasErrors()) {
        // repopulate model if needed
        return "feedback/new";
    }
    feedbackService.createFeedback(request);
    return "redirect:/feedback";
}
```

### 6.2 Global Exception Handling

* Use `@ControllerAdvice` in `web.advice` to:

  * Map domain-specific exceptions (e.g., `FeedbackNotFoundException`, `AccessDeniedException`) to proper error views and status codes.
* Provide custom templates:

  * `error/404.html`
  * `error/403.html`
  * `error/500.html`

---

## 7. Security Guidelines

* Use Spring Security **form login**.
* Restrict routes according to roles:

  * Public: `/`, `/feedback/**`, `/roadmap`, `/auth/**`, static assets.
  * Auth required: feedback CRUD, comment/reply CRUD, upvoting, profile routes.
  * Admin only: `/admin/**`.
* Implement `UserDetailsService` backed by `UserRepository`.
* Use BCrypt for password hashing.
* Ownership checks must be enforced in the **service layer** (e.g., before editing feedback).

---

## 8. Thymeleaf & Frontend Guidelines

* Use a **base layout** (e.g., `layout.html`) with:

  * `<header>` containing logo, navigation, user info.
  * `<main>` with a content block (`th:block` or `th:fragment`).
* Templates under `templates/`:

  * `feedback/list.html`
  * `feedback/detail.html`
  * `feedback/new.html`
  * `feedback/edit.html`
  * `auth/login.html`
  * `auth/register.html`
  * `profile/view.html`
  * `profile/edit.html`
  * `roadmap/index.html`
  * `admin/*.html`
* CSS:

  * Main stylesheet in `static/css/main.css`.
  * Optional partials (e.g., `feedback.css`, `auth.css`) imported from main.

---

## 9. Testing Guidelines

* Unit tests for:

  * Services (business logic and ownership checks).
  * Mappers (where logic is non-trivial).
* Integration tests (optional but recommended):

  * Repository tests (with @DataJpaTest).
  * Web layer tests for critical endpoints.

---

## 10. Using Junie / AI Agents

* Each Junie ticket should:

  * Target a **single layer or clear unit of work** (e.g., “create Feedback DTOs and mapper”, “implement FeedbackService.createFeedback”).
  * Respect these guidelines (entities not exposed in controllers, Flyway-based seeding, DTO usage).
* When generating code:

  * Ask Junie to **only** touch the relevant package to avoid conflicts.
  * Make sure Flyway migrations are updated instead of changing schema via annotations only.

---

```

If you’d like, I can now generate **a matching Flyway migration plan** (concrete V1/V2/V3 files) or **DTO + mapper skeletons** for Feedback/Comment/Reply that follow these guidelines.
::contentReference[oaicite:0]{index=0}
```
