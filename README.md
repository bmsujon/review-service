# Review Service

A Spring Boot application providing REST APIs for managing reviews and comments with nested replies, likes, and dislikes functionality.

## Technical Stack

- **Java**: 21
- **Spring Boot**: 3.4.5
- **Spring Data JPA**: For database interactions
- **PostgreSQL**: As the database
- **Gradle**: 8.x for build automation
- **SpringDoc OpenAPI UI**: 2.7.0 for API documentation
- **Jakarta Validation**: For request validation
- **Lombok**: To reduce boilerplate code
- **JUnit 5**: For testing

## Project Setup

### Prerequisites

- JDK 21
- Gradle 8.x
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
- `review_type`: VARCHAR(255), Type of the review (e.g., COMPANY_REVIEW, PRODUCT_REVIEW)
- `title`: VARCHAR(255), Title of the review
- `content_html`: TEXT, Main content of the review in HTML format
- `ip_address`: VARCHAR(45), IP address of the reviewer
- `like_count`: INTEGER, Number of likes (default: 0)
- `dislike_count`: INTEGER, Number of dislikes (default: 0)
- `has_comment`: BOOLEAN, Indicates if the review has comments (default: FALSE)
- `status`: VARCHAR(50), Status of the review (e.g., PENDING, APPROVED, REJECTED, default: 'PENDING')
- `is_employee`: BOOLEAN, Indicates if the reviewer is an employee (default: FALSE)
- `dept`: VARCHAR(100), Department of the employee (if applicable)
- `role`: VARCHAR(100), Role of the employee (if applicable)
- `company_name`: VARCHAR(255), Name of the company being reviewed
- `website`: VARCHAR(2048), Website of the company
- `work_start_date`: TIMESTAMP WITH TIME ZONE, Work start date for employee reviews
- `work_end_date`: TIMESTAMP WITH TIME ZONE, Work end date for employee reviews
- `created_at`: TIMESTAMP WITH TIME ZONE, Timestamp of creation (default: CURRENT_TIMESTAMP)
- `updated_at`: TIMESTAMP WITH TIME ZONE, Timestamp of last update (default: CURRENT_TIMESTAMP)
- `version`: BIGINT, Version number for optimistic locking (default: 0)

### Comments Table

- `id`: BIGSERIAL, Primary key
- `parent_id`: BIGINT, Foreign key referencing another comment (for replies)
- `review_id`: BIGINT, Foreign key referencing `reviews` (ON DELETE CASCADE)
- `user_name`: VARCHAR(100), Name of the user who commented
- `content`: TEXT, Content of the comment
- `ip_address`: VARCHAR(45), IP address of the commenter
- `like_count`: INTEGER, Number of likes (default: 0)
- `dislike_count`: INTEGER, Number of dislikes (default: 0)
- `status`: VARCHAR(50), Status of the comment (e.g., ACTIVE, HIDDEN, DELETED, default: 'ACTIVE')
- `created_at`: TIMESTAMP WITH TIME ZONE, Timestamp of creation (default: CURRENT_TIMESTAMP)
- `updated_at`: TIMESTAMP WITH TIME ZONE, Timestamp of last update (default: CURRENT_TIMESTAMP)
- `version`: BIGINT, Version number for optimistic locking (default: 0)

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
│   │   ├── controller/       # REST controllers
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── entity/           # JPA entities
│   │   ├── exception/        # Custom exceptions
│   │   ├── model/            # Enums and models
│   │   ├── repository/       # Spring Data JPA repositories
│   │   └── service/          # Business logic
│   └── resources/
│       ├── application.properties  # Configuration
│       └── db_scripts.sql          # Database schema
└── test/
    └── java/com/incognito/reviewservice/  # Unit and integration tests
```

