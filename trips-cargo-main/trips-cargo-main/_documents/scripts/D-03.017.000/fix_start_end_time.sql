update trips_cargo.trips t
set start_time            = (t.start_time + (select (substring(r.time_zone, '([+-]\d+)') || ' hour')::interval
                                             from route_cargo.routelist r
                                             where r.id = t.id)),
    dispatcher_start_time = (t.dispatcher_start_time + (select (substring(r.time_zone, '([+-]\d+)') || ' hour')::interval
                                                        from route_cargo.routelist r
                                                        where r.id = t.id)),
    end_time              = (t.end_time + (select (substring(r.time_zone, '([+-]\d+)') || ' hour')::interval
                                           from route_cargo.routelist r
                                           where r.id = t.id));