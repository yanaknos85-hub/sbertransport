create table if not exists telemechanic.organization_group
(
    id       uuid         not null
        constraint organization_group_pk
            primary key,
    name     varchar(255) not null,
    internal boolean      not null
);

comment on table telemechanic.organization_group is 'Группа организаций';

comment on column telemechanic.organization_group.id is 'Идентификатор записи о группе организаций';

comment on column telemechanic.organization_group.name is 'Наименование группы организаций';

comment on column telemechanic.organization_group.internal is 'Принадлежность к внутренней группе компаний';

create unique index organization_group_name_uindex
    on telemechanic.organization_group (name);

