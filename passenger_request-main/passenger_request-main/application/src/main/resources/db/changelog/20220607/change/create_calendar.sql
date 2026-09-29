create table request.calendar
(
    date_     date        not null
        constraint calendar_pk
            primary key,
    type      varchar(10) not null,
    date_from date
);

create unique index calendar_date__uindex
    on request.calendar (date_);