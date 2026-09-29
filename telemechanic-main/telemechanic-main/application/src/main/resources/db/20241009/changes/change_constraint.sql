alter table telemechanic.medic_request drop constraint medic_request_blood_alcohol_check;
alter table telemechanic.medic_request add constraint medic_request_blood_alcohol_check check (((blood_alcohol >= (0)::double precision) AND (blood_alcohol < (1)::double precision)));

