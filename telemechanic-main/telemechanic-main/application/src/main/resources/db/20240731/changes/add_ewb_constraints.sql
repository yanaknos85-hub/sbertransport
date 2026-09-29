alter table telemechanic.ewb
    add constraint ewb_employee_user_id_fk
        foreign key (author_id) references telemechanic.employee (user_id);

alter table telemechanic.ewb
    add constraint ewb_employee_user_id_fk_2
        foreign key (medic_id) references telemechanic.employee (user_id);

alter table telemechanic.ewb
    add constraint ewb_employee_user_id_fk_3
        foreign key (telemech_out_id) references telemechanic.employee (user_id);

alter table telemechanic.ewb
    add constraint ewb_employee_user_id_fk_4
        foreign key (telemech_in_id) references telemechanic.employee (user_id);

alter table telemechanic.ewb
    add constraint ewb_organization_id_fk
        foreign key (organization_id) references telemechanic.organization;

alter table telemechanic.ewb
    add constraint ewb_request_id_fk
        foreign key (request_id) references telemechanic.request;