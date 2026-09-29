-- Вставка типов и классов транспорта
INSERT INTO request_aggregation.transport_type (transport_type_name, class_names, max_passenger)
VALUES ('TAXI', 'ECONOMY;COMFORT;COMFORT_PLUS;BUSINESS;NONE', 4),
       ('PERSONAL', 'NONE', 4),
       ('PUBLIC', 'NONE', null),
       ('CARSHARING', 'NONE', null),
       ('GROUP_TRANSFER', 'NONE', null);