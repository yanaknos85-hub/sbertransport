create table request.template_for_cargo_history
(
    id                   uuid         not null
        constraint template_for_cargo_history_pkey
            primary key,
    change_date          timestamp    not null,
    code                 integer,
    comment              varchar(255),
    template_status       varchar(255) not null,
    template_for_cargo_id uuid         not null
        constraint fk_template_history_template_for_cargo
            references request.template_for_cargo,
    initiator_id         uuid         not null
        constraint fk_personal_history_employee_template
            references request.employee,
    initiator_description varchar(128) default 'EMPLOYEE'::character varying
);

comment on column request.request_for_cargo_history.initiator_description is 'Тип сотрудника';