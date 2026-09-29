-- noinspection SqlResolveForFile

insert into token_generator.roles (code, data_master)
    (select code, data_master from roles.role where role.exclusive::text ilike '%INTERNAL%');