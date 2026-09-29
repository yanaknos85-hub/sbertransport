CREATE SCHEMA IF NOT EXISTS etrn_cargo;

CREATE TABLE IF NOT EXISTS etrn_cargo.etrn (
    id              UUID PRIMARY KEY,
    humanreadableid VARCHAR(64) NOT NULL UNIQUE,
    status          VARCHAR(64) NOT NULL,
    sender_name     VARCHAR(512),
    receiver_name   VARCHAR(512),
    carrier_name    VARCHAR(512),
    timezone        VARCHAR(64),
    sla             TIMESTAMP,
    current_title   VARCHAR(4),
    active          BOOLEAN DEFAULT TRUE,
    title_chain     JSONB,
    verifications   JSONB,
    lock_info       JSONB,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,
    version         BIGINT DEFAULT 0,
    -- Поля из contract.md раздел 3
    application_number VARCHAR(64),
    route_number       VARCHAR(64),
    cargo_description  VARCHAR(512),
    cargo_places       INTEGER,
    cargo_weight_kg    NUMERIC(14, 2),
    route              VARCHAR(512),
    mrpa_expires_at      DATE,
    cargo_length         NUMERIC(10, 2),
    cargo_width          NUMERIC(10, 2),
    cargo_height         NUMERIC(10, 2),
    ses_full_name        VARCHAR(256),
    ses_role             VARCHAR(128),
    ses_event_datetime   TIMESTAMP,
    ses_event_id         VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS etrn_cargo.etrn_audit (
    id UUID PRIMARY KEY,
    etrn_id UUID NOT NULL REFERENCES etrn_cargo.etrn (id),
    action VARCHAR(512) NOT NULL,
    details VARCHAR(1024),
    created_by UUID,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_etrn_audit_etrn_id ON etrn_cargo.etrn_audit(etrn_id);
CREATE INDEX IF NOT EXISTS idx_etrn_audit_created_at ON etrn_cargo.etrn_audit(created_at DESC);

create table if not exists etrn_cargo.shedlock
(
    name varchar(64)
    constraint shedlock_pkey
    primary key,
    lock_until TIMESTAMP(3) NULL,
    locked_at TIMESTAMP(3) NULL,
    locked_by VARCHAR(255)
);
comment on table etrn_cargo.shedlock is 'Оркестратор планировщиков';
comment on column etrn_cargo.shedlock.name is 'Наименование';
comment on column etrn_cargo.shedlock.lock_until is 'До какого времени заблокирован';
comment on column etrn_cargo.shedlock.locked_at is 'Когда заблокирован';
comment on column etrn_cargo.shedlock.locked_by is 'Кем заблокирован';

CREATE TABLE IF NOT EXISTS etrn_cargo.organization_group (
    id UUID PRIMARY KEY,
    "name"   VARCHAR(255) NOT NULL,
    internal BOOLEAN NOT NULL
);

COMMENT ON TABLE etrn_cargo.organization_group IS 'Группы организаций';

CREATE TABLE IF NOT EXISTS etrn_cargo.organization (
    id UUID PRIMARY KEY,
    digit_id numeric,
    active BOOLEAN,
    official_name VARCHAR(255),
    address TEXT,
    organization_group_id UUID REFERENCES etrn_cargo.organization_group (id)
);

COMMENT ON TABLE etrn_cargo.organization IS 'Организации';

CREATE TABLE IF NOT EXISTS etrn_cargo.department (
    id UUID PRIMARY KEY,
    department_name VARCHAR(255),
    parent_id UUID,
    active BOOLEAN,
    organization_id UUID,
    humanreadableid VARCHAR(128)
);

CREATE INDEX IF NOT EXISTS department_organization_id_idx_manual ON etrn_cargo.department (organization_id);

COMMENT ON TABLE etrn_cargo.department IS 'Подразделение';

CREATE TABLE IF NOT EXISTS etrn_cargo.employee (
    id UUID PRIMARY KEY,
    first_name VARCHAR(256),
    last_name VARCHAR(256),
    patronymic VARCHAR(256),
    personnel_number VARCHAR(32),
    user_id UUID,
    department_id UUID,
    humanreadableid VARCHAR(128),
    active BOOLEAN,
    mobile_phone varchar(255),
    cost_center varchar(128)
);

COMMENT ON TABLE etrn_cargo.employee IS 'Сотрудники';

CREATE TABLE IF NOT EXISTS etrn_cargo.urls (
id uuid NOT NULL,
url text NOT NULL,
pattern text NOT NULL,
"method" text NOT NULL,
CONSTRAINT oto_urls_pattern_uk UNIQUE(url, pattern, method),
CONSTRAINT oto_urls_url_uk UNIQUE(url, method),
CONSTRAINT users_urls_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS etrn_cargo.roles (
    "role" text NOT NULL,
    url_id uuid NOT NULL,
    CONSTRAINT user_roles_ukey UNIQUE(url_id, role),
    CONSTRAINT role_urls_fkey FOREIGN KEY (url_id) REFERENCES etrn_cargo.urls (id)
);
