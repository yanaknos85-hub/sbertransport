alter table exchange_request.request add column if not exists organization_id uuid;

comment on column exchange_request.request.organization_id is 'Организация создателя заявки';