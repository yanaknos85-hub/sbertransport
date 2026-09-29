-- Удаление старого поля и добавление новых в таблице "user"
alter table request_aggregation."users"
drop column contact_info_id;

alter table request_aggregation."users"
    add column fio varchar(255),
add column contact_link_id uuid;

alter table request_aggregation."users"
    add constraint fk_user_contact_link
        foreign key (contact_link_id)
            references request_aggregation.contact_link(id);