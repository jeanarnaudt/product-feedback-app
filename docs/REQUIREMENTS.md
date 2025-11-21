# Product Feedback App – Requirements

Full-stack implementation of the Frontend Mentor “Product Feedback App” challenge using:

- **Backend:** Spring Boot, Spring MVC, Spring Data JPA, Spring Security, Validation
- **Frontend:** Thymeleaf templates, CSS
- **Database:** MySQL
- **Build:** Maven or Gradle

The app is a product feedback board with suggestions, comments, replies, upvotes, and a roadmap.

---

## 1. Goals & Scope

1. Implement the Frontend Mentor Product Feedback App UI and flows with a Spring Boot + Thymeleaf backend.
2. Support authenticated and unauthenticated users with clear role-based permissions.
3. Allow users to:
   - Browse, filter, and sort product feedback.
   - View a roadmap of planned / in-progress / live items.
   - Create, edit, delete feedback (for their own items).
   - Comment and reply to feedback.
   - Upvote feedback (one upvote per feedback per user, togglable).
   - Manage their user profile and avatar.
4. Provide admin/moderator tools for:
   - Managing feedback status and categories.
   - Moderating user content (feedback, comments, replies).
   - Viewing simple statistics about feedback and roadmap.

---

## 2. Tech Stack

- **Java** 17+ (or project default).
- **Spring Boot** (Web, Thymeleaf, Data JPA, Security, Validation).
- **Database:** MySQL.
- **Template Engine:** Thymeleaf.
- **Frontend:** HTML + CSS (following the challenge design).
- **Build Tool:** Maven or Gradle.
- **Optional:** Flyway or Liquibase for DB migrations.
- **JSON parsing:** Jackson for importing `data.json`.

---

## 3. User Roles & Permissions

### 3.1 Visitor (Not Authenticated)

- Can view:
  - List of feedback suggestions.
  - Feedback details, comments, and replies.
  - Roadmap page with feedback grouped by status.
- Can filter and sort feedback.
- Cannot:
  - Create / edit / delete feedback.
  - Add comments or replies.
  - Upvote.
- Any attempt to perform restricted actions must redirect to the login page.

### 3.2 Registered User

Includes all Visitor capabilities, plus:

- **Feedback**
  - Create new feedback.
  - Edit or delete their own feedback only.
  - Choose category when creating/editing.
- **Voting**
  - Upvote or remove upvote on feedback.
  - Exactly one upvote per feedback per user (toggle behaviour).
- **Comments & Replies**
  - Add comments on any feedback.
  - Edit / delete their own comments.
  - Add replies to comments.
  - Edit / delete their own replies.
  - Mention other users via "@username" (optional).
- **Profile**
  - View and edit profile (display name, username, bio).
  - Upload/change avatar (stored as a file path / URL).
  - View a personal activity summary (their feedback and recent comments).

### 3.3 Admin / Moderator

Includes all Registered User capabilities, plus:

- Change feedback status (Suggestion / Planned / In-Progress / Live).
- Edit or delete any feedback, comment, or reply (moderation).
- Manage categories (create, rename, deactivate).
- Access an admin dashboard with:
  - Feedback counts by status and category.
  - List of top-voted suggestions.
  - Recent feedback items and comments.

---

## 4. User Stories (High-Level)

### 4.1 Browsing & Roadmap

- As a visitor, I want to see a list of suggestions so I know what’s being requested.
- As a visitor, I want to filter suggestions by category so I can focus on topics that interest me.
- As a visitor, I want to sort suggestions by upvotes or comments so I can see what’s most important.
- As a visitor, I want to see a roadmap page with Planned, In-Progress, and Live columns so I know what’s coming.

### 4.2 Feedback Management

- As a registered user, I want to create feedback with a title, category, and description.
- As a registered user, I want to edit or delete my own feedback.
- As an admin, I want to change the status of feedback items to Planned/In-Progress/Live.
- As an admin, I want to edit/delete any feedback if it violates rules.

### 4.3 Comments & Replies

- As a registered user, I want to comment on feedback.
- As a registered user, I want to reply to comments and optionally mention users.
- As a registered user, I want to edit or delete my own comments and replies.
- As an admin, I want to remove any inappropriate comments or replies.

### 4.4 Voting

- As a registered user, I want to upvote a piece of feedback.
- As a registered user, I want to remove my upvote if I change my mind.
- The system must ensure that each user can only upvote a specific feedback item once.

### 4.5 Authentication & Profiles

- As a visitor, I want to register and log in so that I can participate (upvote, comment, create feedback).
- As a logged-in user, I want to update my profile and avatar.
- As an admin, I want to log in with elevated privileges.

---

## 5. Functional Requirements

### 5.1 Pages & Routes (Public)

- `/`  
  - Redirects to `/feedback`.
- `/feedback`  
  - Displays list of feedback suggestions.
  - Supports:
    - Sorting (`?sort=upvotes_desc`, `comments_desc`, etc.).
    - Category filtering (`?category=UI`, etc.).
- `/feedback/{id}`  
  - Displays feedback details.
  - Shows current upvotes, comments, and replies.
- `/roadmap`  
  - Shows feedback grouped into columns by status (Planned / In-Progress / Live).
- `/auth/login`  
  - Login form.
- `/auth/register`  
  - Registration form.

### 5.2 Authenticated User Pages

- `/feedback/new`  
  - Create feedback form (title, category, description).
- `/feedback/{id}/edit`  
  - Edit form for own feedback (admin can edit any).
- `/profile`  
  - Shows user profile and their feedback.
- `/profile/edit`  
  - Form to edit profile fields and upload/update avatar.

### 5.3 Admin Pages

- `/admin/dashboard`  
  - Basic metrics:
    - Feedback count by status.
    - Feedback count by category.
    - Top-voted feedback.
- `/admin/feedback`  
  - Table of all feedback with:
    - Status controls.
    - Edit / delete actions.
- `/admin/categories`  
  - Manage categories (create, rename, deactivate).

---

## 6. Data Model Overview

> NOTE: Exact field types and constraints can be defined in JPA entities and migration scripts.

### 6.1 `users`

- `id` (PK)
- `username` (unique)
- `email` (unique)
- `password_hash`
- `role` (`USER`, `ADMIN`)
- `display_name`
- `avatar_url` (nullable)
- `bio` (nullable)
- `created_at`, `updated_at`

### 6.2 `categories`

- `id` (PK)
- `name`
- `slug` (unique)
- `active` (boolean)

### 6.3 `feedback`

- `id` (PK)
- `title`
- `description`
- `status` (`SUGGESTION`, `PLANNED`, `IN_PROGRESS`, `LIVE`)
- `category_id` (FK → `categories`)
- `author_id` (FK → `users`)
- `created_at`, `updated_at`

### 6.4 `comments`

- `id` (PK)
- `content`
- `feedback_id` (FK → `feedback`)
- `author_id` (FK → `users`)
- `created_at`, `updated_at`

### 6.5 `replies`

- `id` (PK)
- `content`
- `comment_id` (FK → `comments`)
- `author_id` (FK → `users`)
- `reply_to_user_id` (nullable FK → `users`, for @mentions)
- `created_at`, `updated_at`

### 6.6 `upvotes`

- `user_id` (FK → `users`)
- `feedback_id` (FK → `feedback`)
- Composite PK: (`user_id`, `feedback_id`)

---

## 7. Authentication & Authorization

- Use Spring Security with form-based login.
- Store users in `users` table with hashed passwords (e.g., BCrypt).
- Authorization rules:
  - Public: `/`, `/feedback/**`, `/roadmap`, `/auth/**`, static assets.
  - Authenticated:
    - Creating/editing/deleting feedback.
    - Creating/editing/deleting comments and replies.
    - Upvoting feedback.
    - Profile pages.
  - Admin:
    - `/admin/**` routes.
    - Edit/delete any feedback/comment/reply.
- Ownership checks:
  - A user can edit/delete only their own feedback, comments, and replies (unless admin).

---

## 8. Validation & Error Handling

- Use Bean Validation annotations on DTOs / entities for:
  - Required fields: title, description, category, comment/reply content.
  - Reasonable length limits (e.g., title 5–100 chars, description ≥ 10 chars).
- Show validation errors in Thymeleaf forms consistent with the design.
- Implement global exception handling:
  - Custom 404 page when feedback/comment is not found.
  - 403 page for forbidden actions.
  - 500 page for unhandled errors.
- Return appropriate HTTP status codes.

---

## 9. Data Seeding (data.json)

- On application startup, if the database is empty:
  - Load `data.json` from `src/main/resources/data/`.
  - Create:
    - Default categories.
    - A default “seed user” (if required for seed feedback).
    - Feedback, comments, and replies based on the JSON data.
- Ensure seeding is idempotent:
  - Only seed when no feedback records exist, or track a simple version flag.

---

## 10. UI & Styling

- Implement the UI to match the Frontend Mentor design as closely as possible:
  - Desktop, tablet, and mobile breakpoints.
  - Hover and active states for buttons, links, and upvote controls.
- Use Thymeleaf templates with a shared base layout:
  - Common header/navigation.
  - Optional sidebar for category filters & roadmap summary.
- Ensure accessibility basics:
  - Semantic HTML.
  - Descriptive labels.
  - Focus states.

---

## 11. Non-Functional Requirements

- **Performance:** Reasonable page load times; avoid N+1 DB queries (use fetch joins where needed).
- **Security:** Hash passwords, protect against CSRF (enable Spring Security defaults), verify authorization on each sensitive operation.
- **Code Quality:** Layered architecture (controller, service, repository); DTOs for web layer; unit tests for core services.
- **Config:** Externalize DB credentials and secrets via environment variables.

---

## 12. Optional Extensions

- Email notifications or in-app notifications for feedback authors when they get new comments.
- Simple search bar to find feedback by title/description.
- Dark mode toggle.
- Activity feed of recent changes.
