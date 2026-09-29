alter table telemechanic.dispatcher
    drop column issue_date;
alter table telemechanic.dispatcher
    drop column expiry_date;
alter table telemechanic.dispatcher
    drop column creation_system;

alter table telemechanic.dispatcher
    add constraint fk_dispatcher_attorney
        foreign key (attorney_id) references telemechanic.attorney(id);