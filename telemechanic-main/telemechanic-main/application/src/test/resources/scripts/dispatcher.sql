insert into telemechanic.attorney (id, number, issue_date, expiry_date, creation_system)
values ('fc1bc338-ecf3-4113-b862-d9bb9d8779c4', '76a3d37e-636c-4a66-8782-6199fe002f25', '2020-01-01', '2030-01-01', 'sys'),
       ('b1e0a979-f4d9-4b01-905b-75b9e5f08a30', 'e723d64b-7a2c-4a74-a475-ce9978f6c2c5', '2020-01-01', '2030-01-01', 'sys'),
       ('eee8b35a-4229-42ef-b029-fea007c42cad', '015f7321-5a2a-4ecd-8f11-3a04ceb159d3', '2000-01-01', '2000-01-02', 'sys');

insert into telemechanic.dispatcher (id, employee_id, organization_id, department_id, attorney_id, active)
values ('e8c396ae-0fba-4a26-a425-0e9073ae71f1', '3cd35c19-fd39-413c-99a0-30f35bd642a8', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',
        '482e6dcb-03a9-4927-90b4-c7081114a9d8', 'eee8b35a-4229-42ef-b029-fea007c42cad', false),
       ('983e4049-e4ac-4639-a782-5a266f4321a8', '3cd35c19-fd39-413c-99a0-30f35bd642a8', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',
        '482e6dcb-03a9-4927-90b4-c7081114a9d8', 'fc1bc338-ecf3-4113-b862-d9bb9d8779c4', true),
       ('0975c771-059c-4705-aafd-57b6748ff527', '7dd56ea0-fa38-400d-93a6-2a4ef4b7df70', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6',
        '482e6dcb-03a9-4927-90b4-c7081114a9d8', 'b1e0a979-f4d9-4b01-905b-75b9e5f08a30', false);