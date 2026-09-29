create index request_status_idx
    on telemechanic.request(status);

create index request_author_id_idx
    on telemechanic.request(author_id);
