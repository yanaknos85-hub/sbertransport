alter table telemechanic.transport
    add column subtype varchar(255) default '-' not null;

alter table telemechanic.transport
    add column type varchar(255) default '-' not null;;

COMMENT ON COLUMN telemechanic.transport.subtype IS 'Тип подтипа ТС';
COMMENT ON COLUMN telemechanic.transport.type IS 'Наименование типа ТС';

alter table telemechanic.transport
    alter column subtype drop default;

alter table telemechanic.transport
    alter column type drop default;