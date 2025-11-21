# 🧩 Junie-Friendly Tickets (copy/paste prompts)

Below are small, clearly scoped prompts you can feed to IntelliJ’s Junie agent. Adjust package names as needed.

---

## Ticket 1 – Initialize Spring Boot Project

> **Prompt for Junie**
> Initialize a new Spring Boot project for the “Product Feedback App” with the following dependencies: Web, Thymeleaf, Spring Data JPA, Spring Security, Validation, MySQL driver, Lombok.
> Use Java 17+ and Maven (or Gradle if the project already exists).
> Create a main package `com.example.productfeedback`.
> Add a basic `application.properties` file with placeholders for MySQL connection and server port.

---

## Ticket 2 – Configure MySQL & Profiles

> **Prompt for Junie**
> Configure the Spring Boot project to connect to a local MySQL database using `application.properties` or `application.yml`.
>
> * Create a `product_feedback` database config (URL, username, password).
> * Enable JPA and show SQL.
> * Use `spring.jpa.hibernate.ddl-auto=update` for now.
> * Optionally add separate `application-dev` and `application-prod` files.

---

## Ticket 3 – Define JPA Entities

> **Prompt for Junie**
> Create JPA entities and repositories for the core domain in `com.example.productfeedback.domain` and `com.example.productfeedback.repository`:
>
> * User
> * Category
> * Feedback
> * Comment
> * Reply
> * Upvote
>   Implement the fields and relationships described in REQUIREMENTS.md (users, categories, feedback, comments, replies, upvotes with composite key).
>   Use `LocalDateTime` for timestamps and annotate relationships with JPA mappings (ManyToOne, OneToMany, etc.).

---

## Ticket 4 – Optional: Flyway/Liquibase Migrations

> **Prompt for Junie**
> Add Flyway (or Liquibase) to manage database schema migrations.
> Create an initial migration that sets up the tables for User, Category, Feedback, Comment, Reply, and Upvote according to the entities.

---

## Ticket 5 – Base Layout & Static Resources

> **Prompt for Junie**
> Set up the Thymeleaf layout and static resources:
>
> * Create `src/main/resources/templates/layout.html` as the base layout with a header and a content block.
> * Configure Thymeleaf to use this layout.
> * Add folders `src/main/resources/static/css` and `static/img`.
> * Add a placeholder `main.css` file and link it from the layout.

---

## Ticket 6 – Data Seeding from data.json

> **Prompt for Junie**
> Implement a data seeding component that runs on application startup:
>
> * Place `data.json` under `src/main/resources/data/`.
> * Use Jackson to parse it.
> * If the database has no feedback records, insert categories, a default seed user (if needed), feedback, comments, and replies based on the JSON.
> * Make the seeding idempotent.

---

## Ticket 7 – Feedback List Page (Controller + View)

> **Prompt for Junie**
> Implement the feedback listing page:
>
> * Create `FeedbackService` and `FeedbackController`.
> * Add a `GET /feedback` endpoint that returns all “suggestion” status feedback items.
> * Support optional `sort` and `category` query parameters.
> * Create `templates/feedback/list.html` using the base layout to display feedback items, upvote count, and comment count.

---

## Ticket 8 – Feedback Detail Page with Comments & Replies

> **Prompt for Junie**
> Implement the feedback detail page:
>
> * Add a `GET /feedback/{id}` endpoint in `FeedbackController`.
> * Fetch a single feedback item with its comments and replies eagerly.
> * Create `templates/feedback/detail.html` that:
>
>   * Shows the feedback info and upvote count.
>   * Lists comments and nested replies.

---

## Ticket 9 – Create/Edit/Delete Feedback (Forms + Validation)

> **Prompt for Junie**
> Implement feedback CRUD for authenticated users:
>
> * Add DTOs and validation annotations for create/edit feedback.
> * Endpoints:
>
>   * `GET /feedback/new` – show create form.
>   * `POST /feedback` – handle create.
>   * `GET /feedback/{id}/edit` – show edit form.
>   * `POST` or `PUT /feedback/{id}` – handle edit.
>   * `POST /feedback/{id}/delete` – delete feedback.
> * Enforce that users can only edit/delete their own feedback (admin can modify any).
> * Show validation errors in the Thymeleaf forms.

---

## Ticket 10 – Comments & Replies CRUD

> **Prompt for Junie**
> Implement creation and editing of comments and replies:
>
> * Endpoints:
>
>   * `POST /feedback/{id}/comments` – add new comment.
>   * `POST /comments/{id}/replies` – add new reply.
>   * Edit/delete endpoints for own comments and replies.
> * Use DTOs with validation for comment/reply content.
> * Update the detail view to include forms and buttons for authenticated users.

---

## Ticket 11 – Upvote Toggle

> **Prompt for Junie**
> Implement upvote toggling for feedback:
>
> * Create `UpvoteService`.
> * Add `POST /feedback/{id}/upvote` endpoint.
> * If the user has not upvoted this feedback, create an upvote record.
> * If the user already upvoted it, remove the record (toggle off).
> * Ensure uniqueness via composite primary key (user_id, feedback_id).
> * Refresh or redirect back to the feedback page after upvoting.

---

## Ticket 12 – Spring Security Configuration

> **Prompt for Junie**
> Configure Spring Security for the Product Feedback App:
>
> * Use form login with `/auth/login` and `/auth/logout`.
> * Allow public access to: `/`, `/feedback/**`, `/roadmap`, `/auth/**`, and static resources.
> * Require authentication for: feedback CRUD, commenting, replying, upvoting, profile pages.
> * Restrict `/admin/**` to ADMIN role.
> * Implement `UserDetailsService` backed by the `users` table with BCrypt password encoding.

---

## Ticket 13 – Authentication Views (Login & Register)

> **Prompt for Junie**
> Implement authentication pages:
>
> * Create `AuthController` with `GET /auth/login`, `GET /auth/register`, and `POST /auth/register`.
> * Implement registration logic to create new users with hashed passwords and default USER role.
> * Create Thymeleaf templates `auth/login.html` and `auth/register.html` styled consistently with the app.

---

## Ticket 14 – Roadmap Page

> **Prompt for Junie**
> Implement the roadmap page:
>
> * Add `GET /roadmap` endpoint.
> * Query feedback grouped by status (PLANNED, IN_PROGRESS, LIVE).
> * Create `templates/roadmap/index.html` showing three columns with feedback cards and counts per column.

---

## Ticket 15 – Profile & Avatar Management

> **Prompt for Junie**
> Implement basic user profile pages:
>
> * Endpoints:
>
>   * `GET /profile` – show current user’s profile and list of their feedback.
>   * `GET /profile/edit` – show edit form.
>   * `POST /profile` – update display name, bio, and avatar.
> * Implement avatar upload handling: store files under `/uploads` and save relative path in `avatar_url`.
> * Update the header/layout to show the logged-in user’s avatar and name.

---

## Ticket 16 – Admin Dashboard & Category Management

> **Prompt for Junie**
> Implement admin features:
>
> * `/admin/dashboard` – show counts of feedback by status and category, and top-voted feedback.
> * `/admin/feedback` – list all feedback with actions to change status and delete items.
> * `/admin/categories` – manage categories (create, rename, deactivate).
> * Enforce ADMIN role on all admin routes.

---

## Ticket 17 – Error Handling & Custom Error Pages

> **Prompt for Junie**
> Implement error handling:
>
> * Add a `@ControllerAdvice` for global exception handling (e.g., entity not found, access denied).
> * Create custom error templates for 404, 403, and 500 pages.
> * Ensure that forbidden actions return 403 and missing resources return 404.

---

## Ticket 18 – CSS & Responsive Design Polish

> **Prompt for Junie**
> Polish the CSS and responsive layout to closely match the Frontend Mentor Product Feedback App design:
>
> * Add responsive breakpoints for mobile, tablet, and desktop.
> * Implement hover and active states for buttons, links, and vote controls.
> * Ensure the feedback list, detail view, and roadmap are visually aligned with the design.

---

If you want, I can next help you:

* Turn specific tickets into even more detailed, step-by-step prompts (e.g., “generate entity code for Feedback only”), or
* Add environment-specific config instructions (local/dev/prod) for this project.
