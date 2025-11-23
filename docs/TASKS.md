Absolutely — let’s slice this into **smaller, more “1–2 Junie calls” sized tasks** instead of big blobs.

I’ll keep the same rough phases, but break each item into tiny, checkable steps.

---

## 0. Project Bootstrapping

### 0.1 Create project & dependencies

* [x] Create Spring Boot project `product-feedback-app`.
* [x] Add dependencies:

  * [x] `spring-boot-starter-web`
  * [x] `spring-boot-starter-thymeleaf`
  * [x] `spring-boot-starter-data-jpa`
  * [x] `spring-boot-starter-security`
  * [x] `spring-boot-starter-validation`
  * [x] `mysql-connector-j`
  * [x] `lombok`
  * [x] `flyway-core`
  * [x] `mapstruct` + annotation processor

### 0.2 Basic configuration

* [x] Create `application.yaml` with:

  * [x] `spring.datasource.url`
  * [x] `spring.datasource.username`
  * [x] `spring.datasource.password`
  * [x] `spring.jpa.hibernate.ddl-auto=none`
  * [x] `spring.flyway.enabled=true`
* [x] Verify app starts and fails only because DB doesn’t exist / isn’t reachable.
* [x] Create `product_feedback_db` database in MySQL.

--- 

## 1. Flyway Migrations & Schema

### 1.1 V1 – Schema

* [x] Create `src/main/resources/db/migration/V1__init_schema.sql`:

  * [x] `users` table.
  * [x] `categories` table.
  * [x] `feedback` table (with `comment_count`, `upvote_count`).
  * [x] `comments` table.
  * [x] `replies` table.
  * [x] `upvotes` table.
  * [x] Indexes for common queries.
* [x] Run app and confirm Flyway applies V1 successfully.
* [x] Inspect DB to confirm tables created correctly.

### 1.2 V2 – Reference data

* [x] Create `V2__seed_reference_data.sql`:

  * [x] Insert default categories: `Feature`, `UI`, `UX`, `Enhancement`, `Bug`.
  * [x] Insert `seeduser` with placeholder password hash.
* [x] Run app; confirm categories and seed user exist.

### 1.3 V3 – Seed from data.json

* [x] Place `data.json` in `src/main/resources/data/data.json`.
* [x] Create `db.migration.V3__SeedInitialFeedbackFromJson` Java migration:

  * [x] Load JSON with Jackson.
  * [x] Find `seeduser` id.
  * [x] Insert feedback based on JSON.
  * [x] Insert comments + replies based on JSON.
* [ ] Run app; confirm seed feedback/comments/replies created correctly.

---

## 2. Domain Model & Repositories

### 2.1 Entities

* [ ] Create `User` entity matching `users` table.
* [ ] Create `Category` entity matching `categories` table.
* [ ] Create `Feedback` entity:

  * [ ] Fields for title, description, status, commentCount, upvoteCount, timestamps.
  * [ ] `@ManyToOne` to `User` (author).
  * [ ] `@ManyToOne` to `Category`.
  * [ ] `@OneToMany` to `Comment`.
* [ ] Create `Comment` entity:

  * [ ] Fields for content, timestamps.
  * [ ] `@ManyToOne` to `Feedback`.
  * [ ] `@ManyToOne` to `User` (author).
  * [ ] `@OneToMany` to `Reply`.
* [ ] Create `Reply` entity:

  * [ ] Fields for content, timestamps.
  * [ ] `@ManyToOne` to `Comment`.
  * [ ] `@ManyToOne` to `User` (author).
  * [ ] `@ManyToOne` to `User` (replyToUser, optional).
* [ ] Create `Upvote` entity (or map via `@IdClass`/`@EmbeddedId`) matching composite PK.

### 2.2 Repositories

* [ ] Create `UserRepository` with:

  * [ ] `Optional<User> findByUsername(String username);`
  * [ ] `Optional<User> findByEmail(String email);`
* [ ] Create `CategoryRepository`.
* [ ] Create `FeedbackRepository` with:

  * [ ] `List<Feedback> findByStatus(String status);`
  * [ ] Methods for filtering by category & sorting (or to be added later).
* [ ] Create `CommentRepository`.
* [ ] Create `ReplyRepository` with:

  * [ ] `long countByCommentId(Long commentId);`
  * [ ] `void deleteByCommentId(Long commentId);`
* [ ] Create `UpvoteRepository` with:

  * [ ] `Optional<Upvote> findByUserIdAndFeedbackId(Long userId, Long feedbackId);`

---

## 3. DTOs & Mappers

### 3.1 Feedback DTOs

* [ ] Create `FeedbackListItemDto`.
* [ ] Create `FeedbackDetailDto`.
* [ ] Create `FeedbackCreateRequest` with validation.
* [ ] Create `FeedbackUpdateRequest` with validation.

### 3.2 Comment & Reply DTOs

* [ ] Create `CommentDto`.
* [ ] Create `CommentCreateRequest`.
* [ ] Create `CommentUpdateRequest`.
* [ ] Create `ReplyDto`.
* [ ] Create `ReplyCreateRequest`.
* [ ] Create `ReplyUpdateRequest`.

### 3.3 MapStruct mappers

* [ ] Create `FeedbackMapper`:

  * [ ] `toListItemDto(Feedback)`.
  * [ ] `toDetailDto(Feedback)`.
  * [ ] `fromCreateRequest(FeedbackCreateRequest)`.
  * [ ] `updateEntityFromRequest(FeedbackUpdateRequest, @MappingTarget Feedback)`.
* [ ] Create `CommentMapper`:

  * [ ] `toDto(Comment)`.
  * [ ] `fromCreateRequest(CommentCreateRequest)`.
  * [ ] `updateEntityFromRequest(CommentUpdateRequest, @MappingTarget Comment)`.
* [ ] Create `ReplyMapper`:

  * [ ] `toDto(Reply)`.
  * [ ] `fromCreateRequest(ReplyCreateRequest)`.
  * [ ] `updateEntityFromRequest(ReplyUpdateRequest, @MappingTarget Reply)`.
* [ ] Configure MapStruct annotation processing and make sure the project builds.

---

## 4. Core Services

### 4.1 UserService

* [ ] Create `UserService`:

  * [ ] `User getCurrentUser()` using `SecurityContext`.
  * [ ] `User registerUser(...)` to be used by AuthService later.
  * [ ] Helper methods to fetch user by id/username.

### 4.2 FeedbackService

* [ ] Implement method: `List<FeedbackListItemDto> getSuggestions(Optional<String> sort, Optional<String> categorySlug)`.
* [ ] Implement method: `FeedbackDetailDto getFeedbackDetail(Long id)`.
* [ ] Implement method: `Long createFeedback(FeedbackCreateRequest request)`:

  * [ ] Map from DTO.
  * [ ] Attach current user.
  * [ ] Attach category.
* [ ] Implement method: `FeedbackUpdateRequest getUpdateForm(Long id)`.
* [ ] Implement method: `void updateFeedback(Long id, FeedbackUpdateRequest request)`:

  * [ ] Resolve category.
  * [ ] Apply changes via mapper.
* [ ] Implement method: `void deleteFeedback(Long id)` with ownership/admin checks (later).

### 4.3 CommentService (with counters)

* [ ] Implement `void addComment(Long feedbackId, CommentCreateRequest request)`:

  * [ ] Load feedback.
  * [ ] Map request to `Comment`.
  * [ ] Set author & timestamps.
  * [ ] Save comment.
  * [ ] `feedback.incrementCommentCount(1)`.
* [ ] Implement `void updateComment(Long commentId, CommentUpdateRequest request)`.
* [ ] Implement `void deleteComment(Long commentId)`:

  * [ ] Load comment & feedback.
  * [ ] Count replies by comment id.
  * [ ] Delete replies (if no cascade).
  * [ ] Delete comment.
  * [ ] `feedback.decrementCommentCount(1 + replyCount)`.

### 4.4 ReplyService (with counters)

* [ ] Implement `void addReply(Long commentId, ReplyCreateRequest request)`:

  * [ ] Load comment & feedback.
  * [ ] Map request to `Reply`.
  * [ ] Set author & timestamps.
  * [ ] Optionally resolve `replyToUser` from username.
  * [ ] Save reply.
  * [ ] `feedback.incrementCommentCount(1)`.
* [ ] Implement `void updateReply(Long replyId, ReplyUpdateRequest request)`.
* [ ] Implement `void deleteReply(Long replyId)`:

  * [ ] Load reply & feedback.
  * [ ] Delete reply.
  * [ ] `feedback.decrementCommentCount(1)`.

### 4.5 UpvoteService

* [ ] Create `UpvoteService` with method `void toggleUpvote(Long feedbackId)`:

  * [ ] Get current user.
  * [ ] Check if upvote exists.
  * [ ] If exists: delete + `feedback.decrementUpvoteCount()`.
  * [ ] If not: create + `feedback.incrementUpvoteCount()`.

---

## 5. Web Layer – Controllers

### 5.1 FeedbackController

* [ ] `GET /feedback` → list using `FeedbackService.getSuggestions`.
* [ ] `GET /feedback/{id}` → detail using `getFeedbackDetail`.
* [ ] `GET /feedback/new` → show `FeedbackCreateRequest` form.
* [ ] `POST /feedback` → create feedback:

  * [ ] Use `@Valid FeedbackCreateRequest`.
  * [ ] Redirect to detail.
* [ ] `GET /feedback/{id}/edit` → show `FeedbackUpdateRequest` form.
* [ ] `POST /feedback/{id}` → update feedback.
* [ ] `POST /feedback/{id}/delete` → delete feedback.
* [ ] `POST /feedback/{id}/upvote` → call `UpvoteService.toggleUpvote` and redirect back.

### 5.2 CommentController

* [ ] `POST /feedback/{feedbackId}/comments`:

  * [ ] Accept `CommentCreateRequest`.
  * [ ] Call `CommentService.addComment`.
* [ ] `POST /comments/{commentId}/edit` (optional, or `PUT` style):

  * [ ] Accept `CommentUpdateRequest`.
  * [ ] Call `CommentService.updateComment`.
* [ ] `POST /comments/{commentId}/delete`:

  * [ ] Accept `feedbackId` as hidden field.
  * [ ] Call `CommentService.deleteComment`.

### 5.3 ReplyController

* [ ] `POST /comments/{commentId}/replies`:

  * [ ] Accept `ReplyCreateRequest`.
  * [ ] Need `feedbackId` as hidden field for redirect.
  * [ ] Call `ReplyService.addReply`.
* [ ] `POST /replies/{replyId}/edit` (optional):

  * [ ] Accept `ReplyUpdateRequest`.
  * [ ] Call `ReplyService.updateReply`.
* [ ] `POST /replies/{replyId}/delete`:

  * [ ] Accept `feedbackId` as hidden field.
  * [ ] Call `ReplyService.deleteReply`.

---

## 6. Authentication & Security

### 6.1 Security Configuration

* [ ] Create `SecurityConfig`:

  * [ ] Permit `"/", "/feedback/**", "/roadmap", "/auth/**", "/css/**", "/js/**", "/img/**"`.
  * [ ] Require authentication for:

    * [ ] Feedback create/edit/delete.
    * [ ] Comment/reply create/edit/delete.
    * [ ] Upvotes.
    * [ ] Profile routes.
  * [ ] Restrict `/admin/**` to `ROLE_ADMIN`.
* [ ] Configure form login at `/auth/login`.
* [ ] Configure logout at `/auth/logout`.

### 6.2 UserDetails & Auth

* [ ] Implement `CustomUserDetails` and `UserDetailsService` using `UserRepository`.
* [ ] Wire password encoding with BCrypt (`PasswordEncoder` bean).
* [ ] Implement `AuthController`:

  * [ ] `GET /auth/login` view.
  * [ ] `GET /auth/register` view.
  * [ ] `POST /auth/register` → create user via `UserService`.

---

## 7. Views & Thymeleaf

### 7.1 Base layout & shared stuff

* [ ] Create `layout.html` with:

  * [ ] Header (logo, nav, login/logout/profile).
  * [ ] Content block/fragment.
* [ ] Ensure all pages extend/include layout.

### 7.2 Feedback views

* [ ] `feedback/list.html`:

  * [ ] Render `FeedbackListItemDto` list.
  * [ ] Show `commentCount` and `upvoteCount`.
  * [ ] Include filter + sort UI.
* [ ] `feedback/detail.html`:

  * [ ] Render `FeedbackDetailDto`.
  * [ ] Render nested `CommentDto` + `ReplyDto`.
  * [ ] Include forms:

    * [ ] Add comment.
    * [ ] Add reply under each comment.
* [ ] `feedback/new.html` and `feedback/edit.html`:

  * [ ] Bind to `FeedbackCreateRequest` / `FeedbackUpdateRequest`.
  * [ ] Show validation errors.

### 7.3 Auth & profile views

* [ ] `auth/login.html`.
* [ ] `auth/register.html`.
* [ ] `profile/view.html`:

  * [ ] Show user info and list of their feedback.
* [ ] `profile/edit.html`:

  * [ ] Form for display name, bio, avatar upload.

### 7.4 Roadmap & admin

* [ ] `roadmap/index.html`:

  * [ ] Show feedback grouped by status.
* [ ] `admin/dashboard.html`.
* [ ] `admin/feedback.html`.
* [ ] `admin/categories.html`.

---

## 8. Validation, Error Handling, and Polish

### 8.1 Validation & errors

* [ ] Ensure all request DTOs have `@Valid` in controllers.
* [ ] Add `GlobalExceptionHandler`:

  * [ ] Handle not-found exceptions (404).
  * [ ] Handle access denied (403).
  * [ ] Handle generic exceptions (500).
* [ ] Create `error/404.html`, `error/403.html`, `error/500.html`.

### 8.2 Styling & responsiveness

* [ ] Create `static/css/main.css`.
* [ ] Implement desktop layout matching Frontend Mentor.
* [ ] Add tablet breakpoints.
* [ ] Add mobile breakpoints.
* [ ] Implement hover/focus/active states.
* [ ] Quick accessibility pass (labels, alt text, focus).

### 8.3 Testing & cleanup

* [ ] Write unit tests for:

  * [ ] `FeedbackService` core methods.
  * [ ] `CommentService` and `ReplyService` counter logic.
* [ ] Write basic repository tests.
* [ ] Manual QA: run through all main flows.
* [ ] Remove unused code, TODOs, and debug logs.

---

If you want, next I can turn any *single* section (for example “3. DTOs & Mappers”) into a **set of ultra-specific Junie prompts**, like “generate the FeedbackListItemDto class exactly like this…” so you can just fire them off one by one.
