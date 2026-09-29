--Ссылки на внешние сервисы autoservice/dispatcher, на ифт для тестирования указываем на внутренние сервисы, тк нет второго инстанса
--При настройке на ПРОМ необходимо указать соответствующие ссылки на сервисы в ДЗО
insert into configs.properties ("key", value, application, profile, "label") values('integration.autoserviceExternalClientUrl', 'http://autoservice:8080', 'contractor', null, 'master');
insert into configs.properties ("key", value, application, profile, "label") values('integration.dispatcherExternalClientUrl', 'http://dispatcher:8080', 'contractor', null, 'master');
--Ссылки для интеграции с внешним сервисом
insert into configs.properties ("key", value, application, profile, "label") values('integration.dispatcherPassengerExternalUrl', 'Ссылка для интеграции с ДЗО пассажиры', 'contractor', null, 'master');
insert into configs.properties ("key", value, application, profile, "label") values('integration.dispatcherCargoExternalUrl', 'Ссылка для интеграции ДЗО cargo', 'contractor', null, 'master');
