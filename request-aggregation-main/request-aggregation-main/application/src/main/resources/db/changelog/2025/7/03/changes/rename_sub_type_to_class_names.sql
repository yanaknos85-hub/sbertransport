-- Переименование поля sub_type_name в class_names
ALTER TABLE request_aggregation.transport_type RENAME COLUMN sub_type_name TO class_names;

-- Обновление комментария к колонке
COMMENT
ON COLUMN request_aggregation.transport_type.class_names IS 'Названия классов транспорта (разделенные точкой с запятой)';
