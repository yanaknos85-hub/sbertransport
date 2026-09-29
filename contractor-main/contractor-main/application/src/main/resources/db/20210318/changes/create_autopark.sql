CREATE TABLE IF NOT EXISTS contractors.autopark
(
    id               UUID PRIMARY KEY,
    autopark_name    VARCHAR NOT NULL,
    active_cars      integer NOT NULL DEFAULT 0
);

COMMENT ON COLUMN contractors.autopark.id IS 'ID автопарка';
COMMENT ON COLUMN contractors.autopark.autopark_name IS 'Название автопарка';
COMMENT ON COLUMN contractors.autopark.active_cars IS 'Количество активных машин в автопарке';