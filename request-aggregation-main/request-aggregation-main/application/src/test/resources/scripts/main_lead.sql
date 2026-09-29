-- MAIN_LEAD
INSERT INTO request_aggregation.main_lead
(id, status, create_date_time, is_application)
VALUES
('8c655b5e-590b-4a78-8a8d-0b9e7c6d5e4f', 'GENERATING', '2025-08-19T19:00:00', true);
-- LEAD_1, LEAD_2
INSERT INTO request_aggregation.lead
(id, status, create_date_time, trip_type, transport_type, "comment", departure_time, "cost", is_driver, transport_class,
main_lead_id, employee_id)
VALUES
('550e8400-e29b-41d4-a716-446655440000', 'PROCESSED', '2025-08-19T19:00:00', 'DAYTIME_TRIP', 'TAXI',
 'Comment', '2025-08-20T19:00:00', 500, false, 'COMFORT', '8c655b5e-590b-4a78-8a8d-0b9e7c6d5e4f', '3cd35c19-fd39-413c-99a0-30f35bd642a8'),
('123e4567-e89b-12d3-a456-426614174000', 'PROCESSED', '2025-08-19T19:00:00', 'DAYTIME_TRIP', 'TAXI',
 'Comment', '2025-08-20T19:00:00', 500, false, 'COMFORT', '8c655b5e-590b-4a78-8a8d-0b9e7c6d5e4f', '3cd35c19-fd39-413c-99a0-30f35bd642a8');
-- POINTS
INSERT INTO request_aggregation.point_lead
(id, main_lead_id, lead_id, type_point, longitude, latitude, waypoint, point_number)
VALUES
('a1b2c3d4-e5f6-41d4-a716-446655440000', '8c655b5e-590b-4a78-8a8d-0b9e7c6d5e4f', '550e8400-e29b-41d4-a716-446655440000',
 'START', 37.6173, 55.7558, 'Moscow, Russia', 1),
('b2c3d4e5-f678-42d3-a817-557766554433', '8c655b5e-590b-4a78-8a8d-0b9e7c6d5e4f', '550e8400-e29b-41d4-a716-446655440000',
 'END', 30.3351, 59.9343, 'Saint Petersburg, Russia', 2),
('c3d4e5f6-7890-43d3-b928-668877665544', '8c655b5e-590b-4a78-8a8d-0b9e7c6d5e4f', '123e4567-e89b-12d3-a456-426614174000',
 'START', 37.6173, 55.7558, 'Moscow, Russia', 1),
('d4e5f678-90ab-44d3-c039-779988776655', '8c655b5e-590b-4a78-8a8d-0b9e7c6d5e4f', '123e4567-e89b-12d3-a456-426614174000',
 'END', 30.3351, 59.9343, 'Saint Petersburg, Russia', 2);