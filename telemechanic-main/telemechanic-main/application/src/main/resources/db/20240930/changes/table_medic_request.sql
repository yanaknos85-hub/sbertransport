create table telemechanic.medic_request
(
    id                uuid        not null
        constraint medic_request_pk
            primary key,
    human_readable_id varchar(16) not null,
    creation_time     timestamp   not null,
    status            varchar(16) not null,
    syst_pressure      int
        check (syst_pressure > 40 and syst_pressure < 300),
    dyast_pressure     int
        check (dyast_pressure > 40 and dyast_pressure < 300),
    pulse             int
        check (pulse > 0 and pulse < 300),
    temperature       float
        check (temperature > 30 and temperature < 47),
    blood_alcohol     float
        check (blood_alcohol > 0 and blood_alcohol < 1),
    comment           varchar(255)
);

comment on table telemechanic.medic_request is 'Таблица с заявками медика';

comment on column telemechanic.medic_request.id is 'ID заявки';

comment on column telemechanic.medic_request.human_readable_id is 'Человекочитаемый идентификатор заявки';

comment on column telemechanic.medic_request.creation_time is 'Дата и время создания заявки';

comment on column telemechanic.medic_request.status is 'Статус заявки';

comment on column telemechanic.medic_request.syst_pressure is 'Систолическое артериальное давление (мм рт. ст)';

comment on column telemechanic.medic_request.dyast_pressure is 'Диастолическое артериальное давление (мм рт. ст)';

comment on column telemechanic.medic_request.pulse is 'Пульс (уд./мин)';

comment on column telemechanic.medic_request.temperature is 'Температура (°С)';

comment on column telemechanic.medic_request.blood_alcohol is 'Алкоголь в крови (Промилле)';

comment on column telemechanic.medic_request.comment is 'Комментарий';

