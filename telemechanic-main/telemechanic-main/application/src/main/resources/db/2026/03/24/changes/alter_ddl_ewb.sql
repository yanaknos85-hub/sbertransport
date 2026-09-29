-- Начало транзакции
begin;

-- Добавление колонки
alter table telemechanic.ewb
    add column if not exists time_zone varchar(9);

-- Добавление комментария
comment on column telemechanic.ewb.time_zone is 'Часовой пояс водителя';

-- Обновление существующих записей (только тех, где time_zone IS NULL)
update telemechanic.ewb
set time_zone = 'UTC+03:00'
where time_zone is null;

-- Проверка, что нет NULL
do $$
declare
    null_count integer;
begin
    select count(*) into null_count
    from telemechanic.ewb
    where time_zone is null;

    if null_count > 0 then
        raise exception 'Cannot set NOT NULL: % row(s) still have NULL time_zone', null_count;
    end if;
end $$;

-- Установка NOT NULL
alter table telemechanic.ewb
    alter column time_zone set not null;

-- Завершение транзакции
commit;

-- Вывод информации о количестве обновленных строк
do $$
declare
    total_count integer;
begin
    select count(*) into total_count from telemechanic.ewb;
    raise notice 'Successfully added time_zone column to % rows', total_count;
end $$;