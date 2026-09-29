alter table telemechanic.ewb_contract
    add start date;

comment on column telemechanic.ewb_contract.start is 'Дата начала действия';

alter table telemechanic.ewb_contract
    add "end" date;

comment on column telemechanic.ewb_contract."end" is 'Дата окончания действия';