with checks_finished_time_from_history as (
    select distinct rh.request_id, max(rh.change_time) over (partition by rh.request_id) last_time
    from telemechanic.request_history rh
    where rh.request_status in ('DONE', 'WARNING'))
update telemechanic.request
set checks_finished_time =  last_time
    from checks_finished_time_from_history
where request_id = request.id;
