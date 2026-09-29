alter table telemechanic.check_photo
    add attempt integer;

comment on column telemechanic.check_photo.attempt is 'Номер попытки';