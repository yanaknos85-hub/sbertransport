alter table telemechanic.ewb add column if not exists qr_code boolean not null default false;

comment on column telemechanic.ewb.qr_code is 'Флаг наличия сгенерированного QR-кода';