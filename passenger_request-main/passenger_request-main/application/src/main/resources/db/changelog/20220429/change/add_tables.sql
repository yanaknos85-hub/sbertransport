create table request.evaluation
(
    id         uuid    not null
        constraint evaluation_pk
            primary key,
    request_id uuid    not null,
    rating     integer not null,
    comment    varchar(255)
);

comment on table request.evaluation is 'Оценка заявки';

comment on column request.evaluation.request_id is 'Id заявки';

comment on column request.evaluation.rating is 'Рейтинг оценки (от 1 до 5)';

comment on column request.evaluation.comment is 'Комментарий к оценке';

create unique index evaluation_id_uindex
    on request.evaluation (id);

create unique index evaluation_request_id_uindex
    on request.evaluation (request_id);

create table request.evaluation_reasons
(
    evaluation_id uuid        not null
        constraint evaluation_reasons_evaluation_id_fk
            references request.evaluation,
    reason        varchar(50) not null,
    primary key (evaluation_id, reason)
);

comment on table request.evaluation_reasons is 'Оценка заявки, причины';