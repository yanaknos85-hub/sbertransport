alter table request_aggregation.lead
    drop constraint lead_status_id_fkey;

alter table request_aggregation.main_lead
    drop constraint status_id_fkey;

alter table request_aggregation.lead
    drop constraint transport_type_id_fkey;

alter table request_aggregation.lead
    drop constraint trip_id_fkey;