with organizations_data (organization_id, edf_operator_id, edf_code, active) as (
    values ('5110ca66-fa90-4c9b-ba34-490c238510fe'::uuid, '2BK', '7707083893-667102008-1027700132195', true),
           ('37c5e6c0-24d9-4914-a230-493f5f428ec0'::uuid, '2BK', '7707083893-667102008-1027700132195', true),
           ('d89ec9a5-9c70-4eff-b672-9ce696c35bca'::uuid, '2BK', '7707083893-667102008-1027700132195', true),
           ('fa69e09f-44cf-402e-b24f-97203e0aacd9'::uuid, '2BK', '7707083893-667102008-1027700132195', true),
           ('3eee23fd-5c1a-42a2-b62d-07e76754977b'::uuid, '2BK', '7707083893-667102008-1027700132195', true),
           ('20bcca7e-855d-4124-9266-b40bb4591618'::uuid, '2BK', '7707083893-667102008-1027700132195', true),
           ('e38ed255-e209-46fb-9523-7382653a5e85'::uuid, '2BK', '7707083893-667102008-1027700132195', true),
           ('27700452-ccc2-4d98-81b6-933e5f5982db'::uuid, '2BK', '7707083893-667102008-1027700132195', true),
           ('42277446-b226-4e86-95a9-c29d185c1163'::uuid, '2BK', '7707083893-667102008-1027700132195', true),
           ('8bb49ec0-437a-42c4-9e74-90f559b4a294'::uuid, '2BK', '7707083893-667102008-1027700132195', true),
           ('9c404ea0-6e68-40f3-8c39-51e8f4d1ff85'::uuid, '2BK', '7707083893-667102008-1027700132195', true)
)
insert
into telemechanic.fleet_owner_organization (organization_id, edf_operator_id, edf_code, active)
select o.organization_id, o.edf_operator_id, o.edf_code, o.active
from organizations_data o
where exists(select 1
             from telemechanic.organization o2
             where o2.id = o.organization_id)
on conflict(organization_id) do nothing;