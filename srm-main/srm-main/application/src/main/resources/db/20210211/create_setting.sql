CREATE TABLE srm.srm_setting
(
    name              varchar(255) PRIMARY KEY,
    value             varchar(255) NOT NULL
);

COMMENT ON TABLE srm.srm_setting is 'Таблица настроек';
COMMENT ON COLUMN srm.srm_setting.name is 'Название';
COMMENT ON COLUMN srm.srm_setting.value is 'Значение';

INSERT INTO srm.srm_setting (name, value) VALUES ('DISTANCE_DEVIATION_KM', '1');
INSERT INTO srm.srm_setting (name, value) VALUES ('TIME_DEVIATION_MIN', '10');
INSERT INTO srm.srm_setting (name, value) VALUES ('SAVING_DEVIATION_PROCENT', '20');
INSERT INTO srm.srm_setting (name, value) VALUES ('MIN_CANCEL_TIME_MIN', '30');
INSERT INTO srm.srm_setting (name, value) VALUES ('MATCH_BY_DISTANCE_AND_TIME', 'true');
