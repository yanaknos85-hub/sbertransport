update notifications_request.trip tr
set time_zone = (select time_zone from request.request_for_group_transfer  r where tr.id = r.id )
where tr.transport_type in ('GROUP_TRANSFER');