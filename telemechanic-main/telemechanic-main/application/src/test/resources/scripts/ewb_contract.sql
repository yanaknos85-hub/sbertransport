insert into telemechanic.ewb_contract
(contract_id, organization_id, inspection_type, edf_operator_id, edf_code, organization_medical_license_id, active, start, "end")
values
('5627f0b0-cac0-49e2-be7f-db026fa42435', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'MEDIC', '2AE', '444444',
        'd8586e9a-a50c-4c6f-a581-e01ef501e983', true, current_date, current_date + interval '1 year'),
('20bfb1f4-6099-45e8-8d81-c42df5070763', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6', 'MEDIC', '2BM', '99999',
        'ed82f1ee-6b36-429c-8e6f-37eb0e8b3775', true, current_date, current_date + interval '1 year'),
('e921aa12-0668-4bac-856c-c3d851882911', '621c288d-e348-46e5-a319-cbf61ef1e396', 'TECHNIC', '2AE', '77777',
        null, true, current_date, current_date + interval '1 year'),
('671eee2e-47a5-4f90-9311-06c82a1f1dd6', '621c288d-e348-46e5-a319-cbf61ef1e396', 'TECHNIC', '2AE', '77777',
        null, true, current_date, current_date + interval '1 year');