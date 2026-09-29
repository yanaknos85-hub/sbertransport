alter table telemechanic.request
    add inspector_id uuid;

alter table telemechanic.request
    add inspection_time timestamp;

alter table telemechanic.request
    add constraint request_employee_id_fk2
        foreign key (inspector_id) references telemechanic.employee;

comment on column telemechanic.request.id is 'Идентификатор записи о заявке';

comment on column telemechanic.request.human_readable_id is 'Человекочитаемый идентификатор';

comment on column telemechanic.request.author_id is 'Идентификатор записи о сотруднике, создавшем заявку';

comment on column telemechanic.request.creation_time is 'Дата и время создания заявки';

comment on column telemechanic.request.vehicle_id is 'Идентификатор записи о транспортном средстве';

comment on column telemechanic.request.status is 'Статус заявки';

comment on column telemechanic.request.inspector_id is 'Идентификатор записи о сотруднике, проводившем контроль';

comment on column telemechanic.request.inspection_time is 'Дата и время проведения контроля';

DO
$do$
    declare
        rec record;
    BEGIN
        FOR rec IN select *
                   from telemechanic.request_history
                   where request_status in ('ON_THE_LINE', 'FINISHED', 'DECLINED')
            LOOP
                update telemechanic.request
                set inspector_id    = rec.initiator_id,
                    inspection_time = rec.change_time
                where id = rec.request_id;
            END LOOP;
    END
$do$;