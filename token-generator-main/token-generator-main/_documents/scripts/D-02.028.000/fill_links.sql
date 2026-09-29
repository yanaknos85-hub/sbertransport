insert into token_generator.account_roles (id, role)
    (select account_id, role_code from sudir.account_roles) on conflict do nothing ;