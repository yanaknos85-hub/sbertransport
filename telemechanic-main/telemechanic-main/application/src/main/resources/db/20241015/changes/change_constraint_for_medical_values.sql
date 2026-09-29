alter table telemechanic.medic_request
    drop constraint medic_request_blood_alcohol_check;

alter table telemechanic.medic_request
    add constraint medic_request_blood_alcohol_check
    check ((blood_alcohol >= (0)::double precision) AND (blood_alcohol <= (0.5)::double precision));

alter table telemechanic.medic_request
    drop constraint medic_request_dyast_pressure_check;

alter table telemechanic.medic_request
    add constraint medic_request_dyast_pressure_check
    check ((dyast_pressure >= 40) AND (dyast_pressure <= 300));

alter table telemechanic.medic_request
    drop constraint medic_request_syst_pressure_check;

alter table telemechanic.medic_request
    add constraint medic_request_syst_pressure_check
    check ((syst_pressure >= 40) AND (syst_pressure <= 300));

alter table telemechanic.medic_request
    drop constraint medic_request_pulse_check;

alter table telemechanic.medic_request
    add constraint medic_request_pulse_check
    check ((pulse >= 0) AND (pulse <= 300));

alter table telemechanic.medic_request
    drop constraint medic_request_temperature_check;

alter table telemechanic.medic_request
    add constraint medic_request_temperature_check
    check ((temperature >= (0)::double precision) AND (temperature <= (46.5)::double precision));