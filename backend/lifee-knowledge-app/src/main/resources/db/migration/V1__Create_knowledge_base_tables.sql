-- 创建知识库表
CREATE TABLE knowledge_bases (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    owner_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_knowledge_bases_owner_name UNIQUE (owner_id, name)
);

-- 创建文档类型枚举
CREATE TYPE document_type AS ENUM (
    'MARKDOWN',
    'TEXT', 
    'PDF',
    'WORD',
    'HTML',
    'JSON',
    'XML'
);

-- 创建文档表
CREATE TABLE documents (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    type document_type NOT NULL DEFAULT 'TEXT',
    knowledge_base_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_documents_knowledge_base FOREIGN KEY (knowledge_base_id) REFERENCES knowledge_bases(id) ON DELETE CASCADE,
    CONSTRAINT uk_documents_knowledge_base_title UNIQUE (knowledge_base_id, title)
);

-- 创建索引
CREATE INDEX idx_knowledge_bases_owner_id ON knowledge_bases(owner_id);
CREATE INDEX idx_knowledge_bases_created_at ON knowledge_bases(created_at);
CREATE INDEX idx_documents_knowledge_base_id ON documents(knowledge_base_id);
CREATE INDEX idx_documents_created_at ON documents(created_at);
CREATE INDEX idx_documents_type ON documents(type);

-- 添加注释
COMMENT ON TABLE knowledge_bases IS '知识库表';
COMMENT ON COLUMN knowledge_bases.id IS '知识库ID';
COMMENT ON COLUMN knowledge_bases.name IS '知识库名称';
COMMENT ON COLUMN knowledge_bases.description IS '知识库描述';
COMMENT ON COLUMN knowledge_bases.owner_id IS '所有者用户ID';
COMMENT ON COLUMN knowledge_bases.created_at IS '创建时间';
COMMENT ON COLUMN knowledge_bases.updated_at IS '更新时间';

COMMENT ON TABLE documents IS '文档表';
COMMENT ON COLUMN documents.id IS '文档ID';
COMMENT ON COLUMN documents.title IS '文档标题';
COMMENT ON COLUMN documents.content IS '文档内容';
COMMENT ON COLUMN documents.type IS '文档类型';
COMMENT ON COLUMN documents.knowledge_base_id IS '所属知识库ID';
COMMENT ON COLUMN documents.created_at IS '创建时间';
COMMENT ON COLUMN documents.updated_at IS '更新时间';