create table if not exists request.taxi_tariff (
                                        id uuid not null primary key,
                                        humanreadableid varchar(255),
                                        organization_id uuid,
                                        contract_id uuid,
                                        region varchar(255),
                                        region_id uuid,
                                        work_group varchar(255),
                                        deleted boolean not null
);

comment on table request.taxi_tariff is 'Тарифа такси, получаемый из сообщения';
comment on column request.taxi_tariff.id is 'ID тарифа';
comment on column request.taxi_tariff.organization_id is 'ID организации';
comment on column request.taxi_tariff.humanreadableid is 'Человекочитаемый ID';
comment on column request.taxi_tariff.contract_id is 'ID контракта с контрагентом';
comment on column request.taxi_tariff.region is 'Регион';
comment on column request.taxi_tariff.region_id is 'Id Региона';
comment on column request.taxi_tariff.work_group is 'Рабочая группа';
comment on column request.taxi_tariff.deleted is 'Флаг удаления тарифа';

