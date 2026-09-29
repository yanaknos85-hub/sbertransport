update tariff.tariff set region_id=(select id from tariff.messages_geo_zone mgz where id is not null limit 1)
where region_id is null;
alter table tariff.tariff alter region_id set not null;
