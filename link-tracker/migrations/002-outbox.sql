create table if not exists outbox_events (
    id uuid primary key default gen_random_uuid(),
    aggregate_id bigint not null,
    event_type varchar(100) not null,
    payload jsonb not null,
    created_at timestamptz not null default now(),
    published_at timestamptz,
    retry_count int not null default 0
);

create index if not exists idx_outbox_unpublished on outbox_events(created_at) where published_at is null;
