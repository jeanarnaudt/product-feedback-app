[![Frontend Mentor | Product feedback app coding challenge](https://tse1.mm.bing.net/th/id/OIP.ILqrkUX4Q0lygNbbmX7PewHaFb?pid=Api)](https://www.frontendmentor.io/challenges/product-feedback-app-wbvUYqjR6?utm_source=chatgpt.com)

Here’s a concrete plan to build the **Frontend Mentor Product Feedback App** as a **Spring Boot + Thymeleaf + CSS + MySQL** full-stack project, with richer user capabilities than the base challenge.

---

## 1. High-level goals

You’ll build a real product feedback board where:

* Visitors can browse feedback & roadmap.
* Registered users can create, edit, comment, reply, and upvote.
* Admins/moderators can manage all feedback, categories, and roadmap statuses.

Tech stack:

* **Backend:** Spring Boot (Web, Thymeleaf, Spring Data JPA, Validation, Spring Security)
* **Frontend:** Thymeleaf templates + vanilla CSS (or small utility framework if you want)
* **DB:** MySQL
* **Build:** Maven or Gradle

---

## 2. Detailed user capabilities (extended from challenge)

Base challenge capabilities from the brief: ([Frontend Mentor][1])

> Create/read/update/delete feedback, validate forms, sort & filter, comment/reply, upvote, responsive layout, hover states.

You’ll extend that into clear role-based behaviour:

### 2.1 Roles

* **Visitor (not logged in)**

  * View list of feedback suggestions.
  * Filter by category.
  * Sort by upvotes or comments.
  * View roadmap page (Planned / In-Progress / Live).
  * View feedback details, comments, and replies.
  * When trying to upvote / comment / add feedback → redirected to login/register.

* **Registered User**

  * All Visitor permissions, plus:
  * **Feedback**

    * Create new feedback.
    * Edit or delete **only their own** feedback.
    * Set category when creating/editing.
  * **Voting**

    * Upvote / remove upvote (toggle) on any feedback.
    * One upvote per feedback per user (enforced in DB).
  * **Comments & Replies**

    * Add comments on any feedback.
    * Edit / delete **their own** comments.
    * Add replies on comments.
    * Edit / delete **their own** replies.
  * **Profile**

    * View & edit profile (name, username, bio).
    * Upload/change avatar (store filename in DB).
    * See a list of their own feedback and activity summary.

* **Admin / Moderator**

  * All Registered permissions, plus:
  * Change feedback status (Suggestion / Planned / In-Progress / Live).
  * Edit or delete *any* feedback, comment, or reply (moderation).
  * Manage categories (add/rename/disable).
  * View a simple admin dashboard: feedback counts by status/category, top-voted items, latest reports.
  * Optionally: “lock” a feedback item (no further comments) if needed.

---

## 3. Data model (MySQL)

Core tables (simplified):

* **users**

  * `id` (PK)
  * `username` (unique)
  * `email` (unique)
  * `password_hash`
  * `role` (enum/string: `USER`, `ADMIN`)
  * `display_name`
  * `avatar_url`
  * `bio`
  * timestamps

* **categories**

  * `id` (PK)
  * `name` (e.g., “UI”, “UX”, “Feature”, etc.)
  * `slug`
  * `active` (boolean)

* **feedback**

  * `id` (PK)
  * `title`
  * `description`
  * `status` (enum/string: `SUGGESTION`, `PLANNED`, `IN_PROGRESS`, `LIVE`)
  * `category_id` (FK → categories)
  * `author_id` (FK → users)
  * `created_at`, `updated_at`

* **comments**

  * `id` (PK)
  * `content`
  * `feedback_id` (FK → feedback)
  * `author_id` (FK → users)
  * `created_at`, `updated_at`

* **replies**

  * `id` (PK)
  * `content`
  * `comment_id` (FK → comments)
  * `author_id` (FK → users)
  * `reply_to_user_id` (FK → users, nullable – for “@username” feature)
  * `created_at`, `updated_at`

* **upvotes**

  * `user_id` (FK → users)
  * `feedback_id` (FK → feedback)
  * PK: (`user_id`, `feedback_id`) to prevent duplicates.

You can optionally add:

* **audit_log** (actions: created feedback, deleted comment, etc. – nice for admin view).

---

## 4. Pages & endpoint overview

### Public pages

* `/` – Redirect to `/feedback`

* `/feedback`

  * List of feedback suggestions with:

    * Sort dropdown (Most/Least upvotes, Most/Least comments).
    * Category filter.
    * “+ Add Feedback” button (goes to login if not authenticated).

* `/feedback/{id}`

  * Feedback details with metadata.
  * Comments & replies thread.
  * Upvote button.
  * Comment form (login required).

* `/roadmap`

  * Columns for Planned / In-Progress / Live feedback.
  * Count per column.

* `/auth/login`, `/auth/register` – Basic auth pages.

### Authenticated user pages

* `/feedback/new` – Create feedback form.
* `/feedback/{id}/edit` – Edit form (owner or admin).
* `/profile` – See own profile & feedback.
* `/profile/edit` – Update profile & avatar.

### Admin pages

* `/admin/dashboard` – Metrics.
* `/admin/feedback` – List of all feedback with edit/delete, status change.
* `/admin/categories` – CRUD categories.
* Optionally: `/admin/users` – manage users/roles.

---

## 5. Implementation phases

### Phase 0 – Project setup

1. Create Spring Boot project with:

   * `spring-boot-starter-web`
   * `spring-boot-starter-thymeleaf`
   * `spring-boot-starter-data-jpa`
   * `spring-boot-starter-validation`
   * `spring-boot-starter-security`
   * MySQL driver, Lombok
2. Configure `application.properties`/`application.yml` for MySQL.
3. Set up base layout (`layout.html`) and static assets folders (`/static/css`, `/static/img`).

---

### Phase 1 – Domain model & repositories

1. Create JPA entities for **User, Category, Feedback, Comment, Reply, Upvote**.
2. Add relationships + JPA constraints (e.g., cascade deletes from feedback → comments → replies, or handle manually).
3. Create Spring Data repositories for each aggregate (`FeedbackRepository`, `CommentRepository`, etc.).
4. Run migrations (Flyway/Liquibase) or use `spring.jpa.hibernate.ddl-auto=update` while prototyping.

---

### Phase 2 – Data seeding from `data.json`

1. Copy the challenge’s `data.json` into `src/main/resources/data/data.json`. ([Frontend Mentor][1])
2. Write a `DataInitializer` (`@Component` + `ApplicationRunner`) to:

   * Parse JSON (Jackson).
   * Create default categories from JSON.
   * Create a dummy “seed user” for feedback/comments if needed.
   * Insert feedback, comments, replies into DB.
3. Mark seeds with a flag (e.g., `seeded = true`) or use a small “schema_version” table so seeding only runs on empty DB.

---

### Phase 3 – Basic feedback listing UI

1. Implement `FeedbackService` + `FeedbackController`.
2. Build `/feedback` template:

   * Sidenav with categories, roadmap summary counts.
   * Main list of suggestions.
   * Sorting & filtering via query params (`/feedback?sort=upvotes_desc&category=ui`).
3. Add CSS to match the Frontend Mentor design (start desktop first, then adjust tablet/mobile).

---

### Phase 4 – Feedback details, comments & replies

1. Implement `/feedback/{id}` controller method:

   * Fetch feedback with comments and replies.
2. Build Thymeleaf template for the detail page.
3. Implement add comment/reply forms as POST endpoints:

   * `/feedback/{id}/comments`
   * `/comments/{id}/replies`
4. Add validation (Bean Validation + error messages in the form).

---

### Phase 5 – Feedback CRUD + validations

1. Implement forms & endpoints for:

   * `GET /feedback/new`, `POST /feedback`
   * `GET /feedback/{id}/edit`, `POST/PUT /feedback/{id}`
   * `POST /feedback/{id}/delete`
2. Add server-side validation:

   * Title required, min length.
   * Description required, min length.
   * Category required.
3. Display validation messages according to the design.

---

### Phase 6 – Upvotes

1. Implement `UpvoteService`.
2. Add endpoint:

   * `POST /feedback/{id}/upvote` (toggle behaviour).
3. Enforce `upvotes` uniqueness at DB level with composite PK.
4. Recalculate or query upvote count for each feedback and show on both list and detail pages.

---

### Phase 7 – Authentication & authorization

1. Set up `UserDetailsService`, password encoder, and `User` entity integration.
2. Create registration & login forms (`/auth/register`, `/auth/login`).
3. Configure Spring Security:

   * Allow access to `static/**`, `/feedback/**`, `/roadmap`, `/auth/**`.
   * Require authentication for:

     * Creating/editing/deleting feedback.
     * Adding comments/replies.
     * Upvoting.
     * Profile & admin routes.
4. Add role checks:

   * Only admins can access `/admin/**` routes.
   * Only owner or admin can edit/delete a feedback/comment/reply.

---

### Phase 8 – Roadmap & admin features

1. Roadmap page:

   * Query feedback grouped by `status`.
   * Display columns with counts.
2. Admin dashboard:

   * Simple controller returning counts: total feedback, by status, by category.
3. Category management:

   * CRUD UI to maintain categories.

---

### Phase 9 – Profile & UX polish

1. Profile pages:

   * Show user details, avatar, list of their feedback.
   * Edit profile + avatar upload (store in `/uploads` and keep path in DB).
2. CSS polish:

   * Make sure layout is responsive for mobile/tablet like the challenge.
   * Add hover/focus states for all interactive elements.
3. Error handling:

   * Custom error pages for 404/403/500.
   * Graceful handling when feedback/comment not found.

---

### Phase 10 – Nice-to-have extras

If you have time:

* Activity feed: “X commented on your feedback”.
* “Subscribe” to feedback and receive email notifications (or just in-app badges).
* Simple search bar for suggestions.
* Dark mode toggle, respecting system preference.

---

If you’d like, next step I can:

* Turn this into a **requirements document (Markdown)** you can drop into your repo, or
* Break it into **Junie-friendly tickets** (small, clearly scoped tasks) you can feed to IntelliJ’s AI agent.

[1]: https://www.frontendmentor.io/challenges/product-feedback-app-wbvUYqjR6 "Frontend Mentor | Product feedback app coding challenge"
