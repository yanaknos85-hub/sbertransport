create table telemechanic.shedlock
(
    name       varchar(64)
        constraint shedlock_pkey
            primary key,
    lock_until TIMESTAMP(3) NULL,
    locked_at  TIMESTAMP(3) NULL,
    locked_by  VARCHAR(255)
);
comment
    on table telemechanic.shedlock is 'Оркестратор планировщиков';
comment
    on column telemechanic.shedlock.name is 'Наименование';
comment
    on column telemechanic.shedlock.lock_until is 'До какого времени заблокирован';
comment
    on column telemechanic.shedlock.locked_at is 'Когда заблокирован';
comment
    on column telemechanic.shedlock.locked_by is 'Кем заблокирован';