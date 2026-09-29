create unique index if not exists dispatcher_employee_id_active_uindex
    on telemechanic.dispatcher (employee_id, active)
    where active = true;