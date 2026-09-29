alter table request_aggregation.lead
    add constraint fk_lead_main_lead_id
        foreign key (main_lead_id) references request_aggregation.main_lead (id);

alter table request_aggregation.lead
    add constraint fk_lead_employee_id
        foreign key (employee_id) references request_aggregation.employee (id);

