-- isInternal для Банка - true, ДЗО - false
insert into configs.properties ("key", value, application, profile, "label") values('integration.isInternal', 'true', 'contractor', null, 'master');
--url внутреннего сервиса интеграции trips/trips-cargo внутри инстанса банка или дзо
insert into configs.properties ("key", value, application, profile, "label") values('integration.dispatcherPassengerInternalUrl', 'Тут указать ссылку для пассажиров!', 'contractor', null, 'master');
insert into configs.properties ("key", value, application, profile, "label") values('integration.dispatcherCargoInternalUrl', 'Тут указать ссылку для грузов!', 'contractor', null, 'master');