alter table request.template_requests
    add constraint fk_template_requests foreign key (template_id) references request.template_for_cargo;