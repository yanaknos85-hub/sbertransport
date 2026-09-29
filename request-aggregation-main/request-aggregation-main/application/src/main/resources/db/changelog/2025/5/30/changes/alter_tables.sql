alter table request_aggregation.point_lead alter column point_number set not null;

alter table request_aggregation.transport_type alter column sub_type_name set not null;

alter table request_aggregation.transport_type alter column sub_type_name type varchar(255);

alter table request_aggregation.lead add column status_history_id uuid;

alter table request_aggregation.lead
    add constraint status_history_id_fkey
        foreign key (status_history_id) references request_aggregation.status_history;