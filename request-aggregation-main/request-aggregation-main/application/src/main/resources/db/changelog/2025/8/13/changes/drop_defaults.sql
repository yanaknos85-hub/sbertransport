alter table request_aggregation.lead
    alter column status drop default;

alter table request_aggregation.lead
    alter column transport_type drop default;

alter table request_aggregation.lead
    alter column trip_type drop default;

alter table request_aggregation.main_lead
    alter column status drop default;