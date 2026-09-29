

create table telemechanic.check_photo
(
    id           uuid         not null
        constraint check_photo_pkey
            primary key,

    check_id uuid not null
        constraint check_photo_check_id_fk
            references telemechanic.check,
    creation_time timestamp not null,
    attempt int not null
);

comment
    on table telemechanic.check_photo is 'фото проверки';

comment
    on column telemechanic.check_photo.id is 'Идентификатор записи о фото';

comment
    on column telemechanic.check_photo.check_id is 'Идентификатор проверки';

comment
    on column telemechanic.check_photo.creation_time is 'Время сохранения';

comment
    on column telemechanic.check_photo.attempt is 'Номер попытки';