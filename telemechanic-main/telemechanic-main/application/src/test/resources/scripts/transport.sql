-- TRANSPORT 1
INSERT INTO telemechanic.transport (id, state_number, brand, model, status, mileage, subtype, type, fuel_tank_volume)
VALUES ('9b1d882c-f623-446b-af1e-ae9ff1966a0e', 'А777АА777', 'Lada', 'Luxe', 'IN_USE', null, 'SUBTYPE', 'TYPE', 40);

-- TRANSPORT 2
INSERT INTO telemechanic.transport (id, state_number, brand, model, status, mileage, subtype, type, fuel_tank_volume)
VALUES ('21ef078e-4a89-4472-ae67-924adb775385', 'А777АА778', 'Lada', 'Luxe', 'IN_USE', null, 'SUBTYPE', 'TYPE', 30);

-- TRANSPORT 3
INSERT INTO telemechanic.transport (id, state_number, brand, model, status, mileage, subtype, type, fuel_tank_volume)
VALUES ('6e28e855-9be3-498d-91a0-a24733d359d9', 'А778АА777', 'Lada', 'Luxe', 'NOT_IN_USE', null, 'SUBTYPE', 'TYPE', 20);

-- TRANSPORT 4
INSERT INTO telemechanic.transport (id, state_number, brand, model, status, mileage, subtype, type, fuel_tank_volume)
VALUES ('5fedf8df-d19e-44a0-8528-4e3e81b1e55f', 'А777АА779', 'Lada', 'Luxe', 'NOT_IN_USE', null, 'SUBTYPE', 'TYPE', 20);

-- TRANSPORT 5
INSERT INTO telemechanic.transport (id, state_number, brand, model, status, mileage, subtype, type, fuel_tank_volume)
VALUES ('057b4fb4-16ab-4420-a16a-5d177a216901', 'А779АА777', 'Lada', 'Luxe', 'IN_USE', null, 'SUBTYPE', 'TYPE', 20);

-- TRANSPORT 6
INSERT INTO telemechanic.transport (id, state_number, brand, model, status, mileage, subtype, type, fuel_tank_volume, contractor_id, autopark_id)
VALUES ('db6dd8bb-8101-48bc-a9c3-d8466ed5a4ee', 'А779АА779', 'Lada', 'Luxe', 'IN_USE', null, 'SUBTYPE', 'TYPE', 20, '50683ed0-4afc-4a13-b5c7-5064220b9517', '3d30f41d-2186-4d04-b695-ffb6e6be7b87');

-- TRANSPORT_ORGANIZATION
INSERT INTO telemechanic.transport_organization (transport_id, organization_id)
VALUES ('9b1d882c-f623-446b-af1e-ae9ff1966a0e', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'),
    ('21ef078e-4a89-4472-ae67-924adb775385', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'),
    ('6e28e855-9be3-498d-91a0-a24733d359d9', 'cb9f17e7-f658-43ca-a70f-40c1c93ad0a6'),
    ('5fedf8df-d19e-44a0-8528-4e3e81b1e55f', '621c288d-e348-46e5-a319-cbf61ef1e396'),
    ('057b4fb4-16ab-4420-a16a-5d177a216901', '621c288d-e348-46e5-a319-cbf61ef1e396');