update trips.contractors set autoassign = false where id in (
    select id from contractors.contractor where integration_type <> 'DISPATCHER' or integration_type is null
    );