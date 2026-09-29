create type request_aggregation.type_lead_status as enum (
    'PROCESSING',
    'PROCESSED',
    'CANCELED'
);

create type request_aggregation.type_transport_type as enum (
    'TAXI',
    'PERSONAL',
    'PUBLIC',
    'CARSHARING',
    'GROUP_TRANSFER'
);

create type request_aggregation.type_transport_class as enum (
    'ECONOMY',
    'BUSINESS',
    'COMFORT',
    'COMFORT_PLUS',
    'NONE'
);

create type request_aggregation.type_trip_type as enum (
    'DAYTIME_TRIP',
    'HIGHTIME_EMPLOYEE_TRANSPORTATION'
);

create type request_aggregation.type_main_lead_status as enum (
    'GENERATING',
    'GENERATED',
    'IN_REVIEW',
    'CANCELED'
);