alter table request.template_for_cargo add column sender_id uuid
    constraint fk_template_for_cargo_sender
    references request.employee;

alter table request.template_for_cargo add column transport_type varchar(255);