
update notifications_request.trip tr
set status_code = (select status_code from request_cargo_v2.request_for_cargo  rfc where tr.id = rfc.id )
where tr.transport_type in ('DEDICATED', 'COURIER', 'DOMESTIC_COURIER', 'INTERREGIONAL', 'INDIVIDUAL');

update notifications_request.trip tr
set status_code = (select status_code from request_cargo_ext.request_for_cargo  rfc where tr.id = rfc.id )
where tr.transport_type in ('DEDICATED', 'COURIER', 'DOMESTIC_COURIER', 'INTERREGIONAL', 'INDIVIDUAL');

update notifications_request.trip tr
set status_code = (select status_code from request_cargo_ext_v2.request_for_cargo  rfc where tr.id = rfc.id )
where tr.transport_type in ('DEDICATED', 'COURIER', 'DOMESTIC_COURIER', 'INTERREGIONAL', 'INDIVIDUAL');