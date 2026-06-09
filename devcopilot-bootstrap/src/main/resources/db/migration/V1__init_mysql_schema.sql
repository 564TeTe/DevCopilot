create table sys_user (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    username varchar(64) not null,
    password_hash varchar(255) not null,
    display_name varchar(64) not null,
    role varchar(32) not null,
    status varchar(32) not null,
    primary key (id),
    unique key uk_sys_user_username (username)
) engine=InnoDB default charset=utf8mb4;

create table dev_project (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    owner_id bigint not null,
    name varchar(128) not null,
    description varchar(1024),
    status varchar(32) not null,
    primary key (id),
    key idx_dev_project_owner (owner_id)
) engine=InnoDB default charset=utf8mb4;

create table knowledge_base (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    project_id bigint not null,
    name varchar(128) not null,
    description varchar(1024),
    status varchar(32) not null,
    primary key (id),
    key idx_knowledge_base_project (project_id)
) engine=InnoDB default charset=utf8mb4;

create table knowledge_document (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    knowledge_base_id bigint not null,
    file_name varchar(255) not null,
    content_type varchar(128),
    storage_path varchar(1024) not null,
    status varchar(32) not null,
    total_chunks int not null,
    primary key (id),
    key idx_knowledge_document_kb (knowledge_base_id)
) engine=InnoDB default charset=utf8mb4;

create table document_chunk (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    document_id bigint not null,
    knowledge_base_id bigint not null,
    chunk_index int not null,
    content longtext not null,
    token_count int not null,
    primary key (id),
    key idx_document_chunk_document (document_id),
    key idx_document_chunk_kb (knowledge_base_id)
) engine=InnoDB default charset=utf8mb4;

create table ai_session (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    project_id bigint not null,
    user_id bigint not null,
    title varchar(128) not null,
    mode varchar(32) not null,
    knowledge_base_id bigint,
    primary key (id),
    key idx_ai_session_user (user_id),
    key idx_ai_session_project (project_id)
) engine=InnoDB default charset=utf8mb4;

create table ai_message (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    session_id bigint not null,
    role varchar(32) not null,
    content longtext not null,
    primary key (id),
    key idx_ai_message_session (session_id)
) engine=InnoDB default charset=utf8mb4;

create table code_repository (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    project_id bigint not null,
    name varchar(128) not null,
    clone_url varchar(1024) not null,
    default_branch varchar(128) not null,
    status varchar(32) not null,
    indexed_files int not null,
    primary key (id),
    key idx_code_repository_project (project_id)
) engine=InnoDB default charset=utf8mb4;

create table code_index_file (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    repository_id bigint not null,
    file_path varchar(1024) not null,
    language varchar(64) not null,
    content_hash varchar(64) not null,
    summary longtext not null,
    primary key (id),
    key idx_code_index_file_repository (repository_id)
) engine=InnoDB default charset=utf8mb4;

create table pr_analysis (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    project_id bigint not null,
    repository_id bigint,
    title varchar(255) not null,
    source_branch varchar(128),
    target_branch varchar(128),
    diff_content longtext not null,
    status varchar(32) not null,
    risk_level varchar(32) not null,
    summary varchar(1024),
    report longtext,
    primary key (id),
    key idx_pr_analysis_project (project_id),
    key idx_pr_analysis_repository (repository_id)
) engine=InnoDB default charset=utf8mb4;

create table async_task (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    task_type varchar(64) not null,
    business_type varchar(64) not null,
    business_id bigint not null,
    status varchar(32) not null,
    progress int not null,
    message varchar(1024),
    payload longtext,
    error_detail longtext,
    primary key (id),
    key idx_async_task_business (business_type, business_id),
    key idx_async_task_status (status)
) engine=InnoDB default charset=utf8mb4;
