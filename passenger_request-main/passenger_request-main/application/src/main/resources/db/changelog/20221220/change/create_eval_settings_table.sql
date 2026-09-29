create table request.eval_settings
(
    transport_type varchar not null,
    remark_type    varchar not null,
    code           varchar not null,
    title          varchar not null,
    order_num      int     not null,
    image          varchar not null,
    constraint eval_settings_pk
        primary key (transport_type, remark_type, code)
);

comment
on table request.eval_settings is 'Настройки оценки';

comment
on column request.eval_settings.transport_type is 'Тип транспорта';

comment
on column request.eval_settings.remark_type is 'Тип замечания (комментария)';

comment
on column request.eval_settings.code is 'Код преднастроенного замечания (комментария)';

comment
on column request.eval_settings.title is 'Надпись преднастроенного замечания (комментария)';

comment
on column request.eval_settings.image is 'Код иконки преднастроенного замечания (комментария)';