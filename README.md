# Review Service

A Spring Boot application providing REST APIs for managing reviews and comments with nested replies, likes, and dislikes functionality.

## Technical Stack

- **Java**: 21
- **Spring Boot**: 3.4.5
- **Spring Data JPA**: For database interactions
- **PostgreSQL**: As the database
- **Gradle**: 8.13
- **SpringDoc OpenAPI UI**: 2.7.0 for API documentation
- **Jakarta Validation**: For request validation
- **Lombok**: To reduce boilerplate code
- **JUnit 5**: For testing

## Project Setup

### Prerequisites

- JDK 21
- Gradle 8.13
- PostgreSQL

### Steps to Build and Run

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd reviewservice
   ```

2. Configure the database in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/review_feedback_db
   spring.datasource.username=postgres
   spring.datasource.password=root
   ```

3. Build the project:
   ```bash
   ./gradlew clean build
   ```

4. Run the application:
   ```bash
   ./gradlew bootRun
   ```

5. Access the API documentation at:
   [Swagger UI](http://localhost:8080/swagger-ui/index.html)

## API Documentation

### Review Endpoints

**Base path:** `/api/v1/reviews`

- `POST /`
  - Create a new review
  - Request Body: `ReviewCreateRequest`
  - Response: `201 Created`

- `GET /`
  - Retrieve a paginated list of reviews
  - Query Parameters: `companyName`, `reviewType`, `page`, `size`
  - Response: `200 OK`

- `GET /{reviewId}`
  - Retrieve a specific review by its ID
  - Response: `200 OK`

- `PUT /{reviewId}/like`
  - Increment the like count of a review
  - Response: `200 OK`

- `PUT /{reviewId}/dislike`
  - Increment the dislike count of a review
  - Response: `200 OK`

### Comment Endpoints

**Base path:** `/api/v1/reviews/{reviewId}/comments`

- `POST /`
  - Create a comment or reply
  - Request Body: `CommentCreateRequest`
  - Query Parameters: `parentId` (optional, for replies)
  - Response: `201 Created`

- `GET /`
  - Retrieve a paginated list of comments for a review
  - Query Parameters: `page`, `size`
  - Response: `200 OK`

- `PUT /{commentId}/like`
  - Increment the like count of a comment
  - Response: `200 OK`

- `PUT /{commentId}/dislike`
  - Increment the dislike count of a comment
  - Response: `200 OK`

- `GET /{commentId}/replies`
  - Retrieve a paginated list of replies for a comment
  - Query Parameters: `page`, `size`
  - Response: `200 OK`

## Database Schema

### Reviews Table

- `id`: BIGSERIAL, Primary key
- `review_type`: VARCHAR(255), Type of the review (e.g., COMPANY_REVIEW, PRODUCT_REVIEW, SERVICE_REVIEW)
- `title`: VARCHAR(255), Title of the review (NOT NULL)
- `content_html`: TEXT, Main content of the review in HTML format (NOT NULL)
- `ip_address`: VARCHAR(45), IP address of the reviewer
- `like_count`: INTEGER, Number of likes (default: 0)
- `dislike_count`: INTEGER, Number of dislikes (default: 0)
- `status`: VARCHAR(50), Status of the review (e.g., PENDING, APPROVED, REJECTED, default: 'PENDING')
- `is_employee`: BOOLEAN, Indicates if the reviewer is an employee (default: FALSE)
- `dept`: VARCHAR(100), Department of the employee (if applicable)
- `role`: VARCHAR(100), Role of the employee (if applicable)
- `company_name`: VARCHAR(255), Name of the company being reviewed
- `website`: VARCHAR(2048), Website of the company
- `work_start_date`: TIMESTAMP WITH TIME ZONE, Work start date for employee reviews
- `work_end_date`: TIMESTAMP WITH TIME ZONE, Work end date for employee reviews
- `reviewer_name`: VARCHAR(100), Name of the reviewer (default: 'Anonymous')
- `created_by`: UUID, ID of the user who created the record
- `updated_by`: UUID, ID of the user who last updated the record
- `created_at`: TIMESTAMP WITH TIME ZONE, Timestamp of creation (default: CURRENT_TIMESTAMP)
- `updated_at`: TIMESTAMP WITH TIME ZONE, Timestamp of last update (default: CURRENT_TIMESTAMP)
- `version`: INT, Version number for optimistic locking (default: 1)

### Comments Table

- `id`: BIGSERIAL, Primary key
- `parent_id`: BIGINT, Foreign key referencing another comment (for replies)
- `review_id`: BIGINT, Foreign key referencing `reviews` (ON DELETE CASCADE)
- `user_name`: VARCHAR(100), User identifier (e.g., system username, if applicable)
- `content`: TEXT, Content of the comment (NOT NULL)
- `ip_address`: VARCHAR(45), IP address of the commenter
- `like_count`: INTEGER, Number of likes (default: 0)
- `dislike_count`: INTEGER, Number of dislikes (default: 0)
- `status`: VARCHAR(50), Status of the comment (e.g., ACTIVE, HIDDEN, DELETED, default: 'ACTIVE')
- `commenter_name`: VARCHAR(100), Display name of the commenter (default: 'Anonymous')
- `created_by`: UUID, ID of the user who created the record
- `updated_by`: UUID, ID of the user who last updated the record
- `created_at`: TIMESTAMP WITH TIME ZONE, Timestamp of creation (default: CURRENT_TIMESTAMP)
- `updated_at`: TIMESTAMP WITH TIME ZONE, Timestamp of last update (default: CURRENT_TIMESTAMP)
- `version`: INT, Version number for optimistic locking (default: 1)

## Testing

Run the tests using:
```bash
./gradlew test
```

## Folder Structure

```
src/
├── main/
│   ├── java/com/incognito/reviewservice/
│   │   ├── ReviewserviceApplication.java # Main application class
│   │   ├── config/           # Application configuration (e.g., WebConfig)
│   │   ├── controller/       # REST API controllers
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── entity/           # JPA entities (database tables)
│   │   ├── exception/        # Custom exception classes and handlers
│   │   ├── model/            # Enums, constants, and other model classes
│   │   ├── repository/       # Spring Data JPA repositories
│   │   └── service/          # Business logic services
│   └── resources/
│       ├── application.properties  # Application configuration properties
│       └── db_scripts.sql          # SQL scripts for database schema (if used)
└── test/
    └── java/com/incognito/reviewservice/ # Test sources
        ├── controller/       # Tests for controllers
        ├── exception/        # Tests for exception handlers
        ├── service/          # Tests for services
        └──                   # Other tests (e.g., ReviewserviceApplicationTests.java)
```

## Future Enhancements

The following features are planned for future development to transform the service into an enterprise-grade platform for internal feedback and reviews:

**I. Foundational Enhancements: User, Company, and Role Management**

1.  **Data Model Expansion:**
    *   **Company Entity:** Introduce a `Company` entity to represent different companies within the enterprise.
    *   **User Entity Enhancement:**
        *   Associate users with a `Company`.
        *   Add fields for user roles (e.g., `EMPLOYEE`, `ADMIN`, `COMPANY_ADMIN`).
        *   Securely store authentication details.
    *   **Review & Comment Entity Modification:**
        *   Link reviews/comments to the `User` who created them.
        *   Add an `isAnonymous` flag.
        *   Add a `status` field (e.g., `PENDING_APPROVAL`, `PUBLISHED`, `HIDDEN`, `REJECTED`).
        *   Associate reviews/comments with the `Company` of the posting user.

2.  **Authentication and Authorization:**
    *   Implement robust user authentication (e.g., Spring Security with JWT or OAuth2).
    *   Develop role-based authorization to control access.

3.  **API Development for Core Entities:**
    *   **Company Management API:** CRUD operations for companies (super-admin restricted).
    *   **User Management API:** User registration (with company association), profile updates.
    *   **Role Management API:** Assign/revoke user roles (admin-only).

**II. Core Feature Implementation: Posting and Moderation**

1.  **Content Submission Flow:**
    *   Modify review/comment creation endpoints to:
        *   Capture authenticated user ID.
        *   Allow anonymous posting option.
        *   Set initial post `status` based on admin configuration.

2.  **Content Moderation System:**
    *   **Admin Configuration API:**
        *   Endpoints for admins to set rules (e.g., mandatory verification for posts).
    *   **Moderation API:**
        *   Endpoints for admins to view, approve, reject, hide, or unhide posts.

3.  **Content Visibility and Retrieval:**
    *   Update review/comment retrieval APIs:
        *   Ensure only `PUBLISHED` content is visible to regular employees (unless viewing their own non-published posts).
        *   Allow admins to view content with other statuses.
        *   Mask user details for anonymous posts.
        *   Consider company-based filtering or cross-company visibility.

**III. Supporting Tasks**

1.  **Database Schema Updates:**
    *   Update `db_scripts.sql` or implement migrations (e.g., Flyway/Liquibase).

2.  **Service Layer Logic:**
    *   Implement business logic for all new features in the service layer.

3.  **API Documentation:**
    *   Update OpenAPI/Swagger documentation for all changes.

4.  **Testing:**
    *   Write comprehensive unit and integration tests for new functionalities.
