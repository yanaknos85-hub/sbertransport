update tariff.tariff
set transport_type='TAXI'
where transport_type is null
  and tariff.tariff.transport_type_id = '7f18ce71-99a7-47b5-b285-335058c6715c';

update tariff.tariff
set transport_type='PERSONAL'
where transport_type is null
  and tariff.tariff.transport_type_id = '1a33601d-4db4-4720-8d09-95f015770fe0';

update tariff.tariff
set transport_type='PUBLIC'
where transport_type is null
  and tariff.tariff.transport_type_id = ('fa96da51-068d-4cf5-bfd2-4cd28d717e13');
