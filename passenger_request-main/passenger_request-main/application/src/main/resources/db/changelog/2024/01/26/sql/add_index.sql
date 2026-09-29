create index request_for_taxi_request_closed_datetime_active_index
    on request.request_for_taxi (request_closed_datetime, active) where request_closed_datetime is null and active = true;