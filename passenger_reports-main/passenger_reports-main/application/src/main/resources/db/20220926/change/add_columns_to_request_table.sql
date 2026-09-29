alter table reports.request add column deadline_state varchar;
alter table reports.request add column is_sla_expired boolean;
comment on column reports.request.deadline_state is 'Индикатор котрольного срока: NONE, YELLOW, RED';
comment on column reports.request.is_sla_expired is 'Флаг просрочки SLA';