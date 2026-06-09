create extension if not exists vector;

create table if not exists kb_chunk_embedding (
    chunk_id bigint primary key,
    knowledge_base_id bigint not null,
    document_id bigint not null,
    embedding vector(64) not null,
    content text not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_kb_chunk_embedding_kb on kb_chunk_embedding (knowledge_base_id);
create index if not exists idx_kb_chunk_embedding_vector on kb_chunk_embedding using hnsw (embedding vector_cosine_ops);

create table if not exists code_file_embedding (
    file_id bigint primary key,
    repository_id bigint not null,
    file_path text not null,
    embedding vector(64) not null,
    summary text not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_code_file_embedding_repository on code_file_embedding (repository_id);
create index if not exists idx_code_file_embedding_vector on code_file_embedding using hnsw (embedding vector_cosine_ops);
