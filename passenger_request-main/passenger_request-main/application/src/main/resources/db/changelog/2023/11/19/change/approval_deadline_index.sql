create index if not exists request_request_for_taxi_approval_deadline_idx
    on request.request_for_taxi (approval_deadline)
where request_status = 'TAXI_AWAITING_APPROVAL';

create index if not exists request_request_for_personal_approval_deadline_idx
    on request.request_for_personal (approval_deadline)
    where request_status = 'PERSONAL_AWAITING_APPROVAL';

create index if not exists request_request_for_public_approval_deadline_idx
    on request.request_for_public (approval_deadline)
    where request_status = 'PUBLIC_AWAITING_APPROVAL';

create index if not exists request_request_for_carsharing_approval_deadline_idx
    on request.request_for_carsharing (approval_deadline)
    where request_status = 'CARSHARING_AWAITING_APPROVAL';