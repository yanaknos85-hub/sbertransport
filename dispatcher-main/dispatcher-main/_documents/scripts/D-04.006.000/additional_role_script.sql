insert into roles ."role" (code, "name", description, data_master, default_for, "exclusive")
values ('ROLE_DISPATCHER_ROOM_ADMIN',
'Администратор диспетчерской',
'Роль, которая позваоляет пользователю переключаться между разными диспетчерскими и производить административные манипуляции',
true,
'[]'::json,
'["EXTERNAL","INTERNAL"]'::json)

insert into authentication."role" (code, "name", description, data_master, default_for)
values ('ROLE_DISPATCHER_ROOM_ADMIN',
'Администратор диспетчерской',
'Роль, которая позваоляет пользователю переключаться между разными диспетчерскими и производить административные манипуляции',
true,
'[]'::json)

insert into sudir."role" (code, "name", description, data_master, default_for)
values ('ROLE_DISPATCHER_ROOM_ADMIN',
'Администратор диспетчерской',
'Роль, которая позваоляет пользователю переключаться между разными диспетчерскими и производить административные манипуляции',
true,
'[]'::json)