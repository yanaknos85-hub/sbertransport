alter table telemechanic.ewb
    add medic_decision_time timestamp;

comment on column telemechanic.ewb.medic_decision_time is 'Дата и время принятия решения медика';
