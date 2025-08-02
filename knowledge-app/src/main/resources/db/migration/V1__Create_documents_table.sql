CREATE TABLE documents (
    id VARCHAR(255) PRIMARY KEY,
    path VARCHAR(1000) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    file_info TEXT NOT NULL,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    last_modified TIMESTAMP NOT NULL
);

CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_documents_path ON documents(path);
CREATE INDEX idx_documents_last_modified ON documents(last_modified);