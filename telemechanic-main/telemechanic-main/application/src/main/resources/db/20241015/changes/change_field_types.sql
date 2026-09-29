alter table telemechanic.medic_request drop column temperature;
alter table telemechanic.medic_request drop column blood_alcohol;

alter table telemechanic.medic_request add column temperature numeric;
alter table telemechanic.medic_request add column blood_alcohol numeric;

alter table telemechanic.medic_request
    add constraint medic_request_blood_alcohol_check
    check ((blood_alcohol >= (0)::double precision) AND (blood_alcohol <= (0.5)::double precision));
alter table telemechanic.medic_request
    add constraint medic_request_temperature_check
    check ((temperature >= (0)::double precision) AND (temperature <= (46.5)::double precision));