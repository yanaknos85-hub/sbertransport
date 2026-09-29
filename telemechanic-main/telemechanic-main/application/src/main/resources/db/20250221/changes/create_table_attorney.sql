create table telemechanic.attorney (
    id uuid primary key not null,
    telemechanic_id uuid not null,
    attorney_id uuid not null,
    issue_date timestamp not null,
    expiry_date timestamp not null,
    creation_system varchar(150) not null,
    constraint attorney_telemechanic_id_fkey foreign key (telemechanic_id) references telemechanic.employee (id)
);

comment on column telemechanic.attorney.id is 'Идентификатор записи МЧД';
comment on column telemechanic.attorney.telemechanic_id is 'Идентификатор телемеханика';
comment on column telemechanic.attorney.attorney_id is 'Номер доверености';
comment on column telemechanic.attorney.issue_date is 'Дата выдачи';
comment on column telemechanic.attorney.expiry_date is 'Дата окончания срока действия';
comment on column telemechanic.attorney.creation_system is 'Система создания';