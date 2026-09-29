insert into authentication."role"
(code, name, description, data_master, default_for)
(select r.code , r."name" , r.description , r.data_master , r.default_for  from roles."role" r
where r.code ='ROLE_CLIENT_MANAGER');