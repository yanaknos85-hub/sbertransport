alter table request_aggregation.address
    add constraint fk_address_employee_id
        foreign key (employee_id) references request_aggregation.employee (id);