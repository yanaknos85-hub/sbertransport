alter table telemechanic.medic_contractor
rename column sign_key_end_date to sign_key_end_date_time;

alter table telemechanic.medic_contractor
alter column sign_key_end_date_time type timestamp using sign_key_end_date_time::timestamp;