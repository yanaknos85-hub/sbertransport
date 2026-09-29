alter table telemechanic.organization_medical_license
    add issue_date date not null;

alter table telemechanic.organization_medical_license
    add expiry_date date not null;

comment on column telemechanic.organization_medical_license.issue_date is 'Дата выдачи';
comment on column telemechanic.organization_medical_license.expiry_date is 'Дата окончания срока действия';