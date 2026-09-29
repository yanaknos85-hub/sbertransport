alter table request.template_for_cargo add column template_status varchar(255);
alter table request.template_for_cargo add column author_id uuid
    constraint fk_template_for_cargo_author
        references request.employee;
    alter table request.template_for_cargo add column status_code integer;
alter table request.template_for_cargo add column approval_state varchar(255);
alter table request.template_for_cargo add column approved_by_id uuid
        constraint fk_template_for_cargo_approved
            references request.employee;
alter table request.template_for_cargo add column approval_date timestamp;
