update authentication.role
set default_for = '[]'::json
where code = 'ROLE_PARKING_ADMIN_ORGANIZATION';

update sudir.role
set default_for = '[]'::json
where code = 'ROLE_PARKING_ADMIN_ORGANIZATION';

-- Вместо {excludes} укажите список логинов, для которых необходимо оставить роль.
delete from authentication.account_roles
where role_code = 'ROLE_PARKING_ADMIN_ORGANIZATION' and account_id not in (select id from authentication.account where login in ({excludes}));

-- Вместо {excludes} укажите список табельников, для которых необходимо оставить роль.
delete from sudir.account_roles
where role_code = 'ROLE_PARKING_ADMIN_ORGANIZATION' and account_id not in (select id from sudir.account where account.personnel_number in ({excludes}));