drop index if exists idx_users_token_id;
alter table exchange_request.users drop column if exists token_id;