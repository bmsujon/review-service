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

## Database Schema

### Reviews Table

- `id`: Primary key
- `review_type`: Type of the review (e.g., POSITIVE, NEGATIVE, MIXED)
- `title`: Title of the review
- `content`: Main content of the review
- `like_count`: Number of likes
- `dislike_count`: Number of dislikes
- `status`: Status of the review (e.g., PENDING, APPROVED, REJECTED)
- `created_at`: Timestamp of creation
- `updated_at`: Timestamp of last update

### Comments Table

- `id`: Primary key
- `review_id`: Foreign key referencing `reviews`
- `parent_id`: Foreign key referencing another comment (for replies)
- `content`: Content of the comment
- `like_count`: Number of likes
- `dislike_count`: Number of dislikes
- `status`: Status of the comment (e.g., ACTIVE, HIDDEN, DELETED)
- `created_at`: Timestamp of creation
- `updated_at`: Timestamp of last update

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

