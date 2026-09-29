drop index evacuation.employee_organization_id_index;

alter table evacuation.employee
    drop constraint employee_organization_id_fk;

alter table evacuation.employee
    drop column organization_id;