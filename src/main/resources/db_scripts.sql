-- Script to create tables for the ReviewService application
-- Target Database: PostgreSQL

-- Drop tables if they exist to ensure a clean setup (optional, use with caution)
-- DROP TABLE IF EXISTS comments CASCADE;
-- DROP TABLE IF EXISTS reviews CASCADE;

-- Create the 'reviews' table
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    review_type VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    content_html TEXT,
    ip_address VARCHAR(45),
    like_count INTEGER NOT NULL DEFAULT 0,
    dislike_count INTEGER NOT NULL DEFAULT 0,
    has_comment BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    is_employee BOOLEAN NOT NULL DEFAULT FALSE,
    dept VARCHAR(100),
    role VARCHAR(100),
    company_name VARCHAR(255),
    website VARCHAR(2048),
    work_start_date TIMESTAMP WITH TIME ZONE,
    work_end_date TIMESTAMP WITH TIME ZONE,
    reviewer_name VARCHAR(100) DEFAULT 'Anonymous',
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 1
);

-- Add indexes to 'reviews' table
CREATE INDEX idx_reviews_status ON reviews(status);
CREATE INDEX idx_reviews_review_type ON reviews(review_type);
CREATE INDEX idx_reviews_company_name ON reviews(company_name);
CREATE INDEX idx_reviews_created_at ON reviews(created_at);

-- Create the 'comments' table
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT,
    review_id BIGINT NOT NULL,
    user_name VARCHAR(100),
    content TEXT NOT NULL,
    ip_address VARCHAR(45),
    like_count INTEGER NOT NULL DEFAULT 0,
    dislike_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    commenter_name VARCHAR(100) DEFAULT 'Anonymous',
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_comments_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE SET NULL -- Or ON DELETE CASCADE depending on desired behavior for replies
);

-- Add indexes to 'comments' table
CREATE INDEX idx_comments_review_id ON comments(review_id);
CREATE INDEX idx_comments_parent_id ON comments(parent_id);
CREATE INDEX idx_comments_created_at ON comments(created_at);

-- Optional: Add comments to tables and columns for better understanding
COMMENT ON TABLE reviews IS 'Stores review information submitted by users.';
COMMENT ON COLUMN reviews.review_type IS 'Type of the review (e.g., COMPANY_REVIEW, PRODUCT_REVIEW).';
COMMENT ON COLUMN reviews.status IS 'Current status of the review (e.g., PENDING, APPROVED, REJECTED).';

COMMENT ON TABLE comments IS 'Stores comments made on reviews, supporting threaded replies.';
COMMENT ON COLUMN comments.parent_id IS 'ID of the parent comment if this is a reply.';
COMMENT ON COLUMN comments.review_id IS 'ID of the review this comment belongs to.';
COMMENT ON COLUMN comments.status IS 'Current status of the comment (e.g., ACTIVE, HIDDEN, DELETED).';

-- Note on created_at and updated_at:
-- The DEFAULT CURRENT_TIMESTAMP is a common way to handle these at the DB level.
-- If your BaseEntity uses Hibernate's @CreationTimestamp and @UpdateTimestamp,
-- Hibernate will manage these values at the application level upon entity persistence/update.
-- Having DB defaults is a good fallback and ensures these fields are populated even with direct DB inserts.
