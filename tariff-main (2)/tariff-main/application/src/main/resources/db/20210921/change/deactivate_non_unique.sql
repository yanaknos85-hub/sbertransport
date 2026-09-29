update tariff.tariff
set active= false
where transport_type = 'TAXI'
  and active = true
  and id not in (
    SELECT DISTINCT ON (organization_id,contract_id, region_id,taxi_class) tr.id
    from tariff.tariff tr
             join tariff.messages_geo_zone reg on reg.id = tr.region_id
    where tr.transport_type = 'TAXI'
      and active = true);

update tariff.tariff
set active= false
where transport_type = 'PERSONAL'
  and active = true
  and id not in (SELECT DISTINCT ON (organization_id, region_id) tr.id
                 from tariff.tariff tr
                          join tariff.messages_geo_zone reg on reg.id = tr.region_id
                 where tr.transport_type = 'PERSONAL'
                   and active = true);

update tariff.tariff set active= false
where transport_type = 'PUBLIC' and active = true
  and id not in (
    SELECT DISTINCT ON (organization_id, tr.region_id) tr.id
    from tariff.tariff tr
             join tariff.messages_geo_zone reg on reg.id = tr.region_id
    where tr.transport_type = 'PUBLIC'
      and active = true);