alter table srm.srm_request_kpi add column if not exists org_request_id uuid;
comment on column srm.srm_request_kpi.org_request_id is 'Идентификатор изначальной заявки';
update srm.srm_request_kpi set org_request_id = id;
commit;
