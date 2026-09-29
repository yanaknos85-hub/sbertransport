INSERT INTO configs.descriptions ("key",description) VALUES
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.autoAddPartitions','Кафка с реестром схем: Разрешение на автоматическое создание партиций'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.configuration.ssl.client.auth','Кафка с реестром схем: Необходимость авторизации клиента при работе с SSL'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.brokers','Кафка с реестром схем: Список адресов брокеров'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.configuration.ssl.endpoint.identification.algorithm','Кафка с реестром схем: Алгоритм идентификации SSL-эндпоинтов'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.consumerProperties.compression.type','Кафка с реестром схем: Используемый получателями тип сжатия. Допустимые значения см. в документации кафки'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.consumerProperties.key.deserializer','Кафка с реестром схем: Десериализатор ключа'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.consumerProperties.schema.registry.url','Кафка с реестром схем: Адрес реестра схем'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.consumerProperties.specific.avro.reader','Кафка с реестром схем: Особый читатель авро (см. документацию)'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.consumerProperties.standardHeaders','Кафка с реестром схем: Использовать стандартные заголовки'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.consumerProperties.value.deserializer','Кафка с реестром схем: Десериализатор значения');
INSERT INTO configs.descriptions ("key",description) VALUES
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.producerProperties.auto.register.schemas','Кафка с реестром схем: Авторегистрация схем'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.producerProperties.compression.type','Кафка с реестром схем: Тип сжатия отправителем'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.producerProperties.key.serializer','Кафка с реестром схем: Сериализатор ключа'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.producerProperties.schema.reflection','Кафка с реестром схем: Использование рефлексии при использовании схем'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.producerProperties.schema.registry.url','Кафка с реестром схем: Адрес реестра схем'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.producerProperties.value.serializer','Кафка с реестром схем: Сериализатор значения'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.consumer.topic.properties.cleanup.policy','Кафка с реестром схем: Получатель - политика очистки топика'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.consumer.topic.properties.retention.ms','Кафка с реестром схем: Время в милисекундах до применения политики очистки'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.producer.topic.properties.cleanup.policy','Кафка с реестром схем: Отправитель - политика очистки топика'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.producer.topic.properties.retention.ms','Кафка с реестром схем: Время в милисекундах до применения политики очистки');
INSERT INTO configs.descriptions ("key",description) VALUES
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.autoAddPartitions','Кафка: разрешение на автоматическое добавление партиций'),
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.consumerProperties.dlqProducerProperties.key.deserializer','Кафка: десериализатор ключа для очереди ошибочных сообщений'),
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.consumerProperties.key.deserializer','Кафка: десериализатор ключа входящих сообщений'),
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.consumerProperties.standardHeaders','Кафка: использование стандартных заголовков'),
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.consumerProperties.value.deserializer','Кафка: десериализатор значения входящих сообщений'),
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.producerProperties.key.serializer','Кафка: сериализатор ключа исходящих сообщений'),
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.producerProperties.value.serializer','Кафка: сериализатор исходящих сообщений'),
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.default.consumer.topic.properties.cleanup.policy','Кафка: политика очистки брокера'),
	 ('server.port','Рабочий порт приложения'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.consumer.enableDlq','Кафка с реестром схем: Включить очередь сообщений, вызвавших ошибку');
INSERT INTO configs.descriptions ("key",description) VALUES
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.consumer.topic.properties.records.policy.enabled','Кафка с реестром схем: Настройка для server-side проверки соответствия сообщения схеме'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.consumer.topic.properties.records.policy.name','Кафка с реестром схем: Настройка для server-side проверки соответствия сообщения схеме'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.producer.topic.properties.records.policy.name','Кафка с реестром схем: Настройка для server-side проверки соответствия сообщения схеме'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.autoAddPartitions','Кафка SSL: разрешение на автоматическое добавление партиций'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.brokers','Кафка SSL: список брокеров'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.configuration.security.protocol','Кафка SSL: протокол безопасности'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.configuration.ssl.client.auth','Кафка SSL: требование авторизации клиента'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.configuration.ssl.enabled.protocols','Кафка SSL: обслуживаемые TLS-протоколы'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.configuration.ssl.endpoint.identification.algorithm','Кафка SSL: алгоритм идентификации эндпоинтов'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.consumerProperties.dlqProducerProperties.key.deserializer','Кафка SSL: десериализатор ключа для очереди ошибочных сообщений');
INSERT INTO configs.descriptions ("key",description) VALUES
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.consumerProperties.key.deserializer','Кафка SSL: десериализатор ключа'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.consumerProperties.standardHeaders','Кафка SSL: использование стандартных заголовков'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.consumerProperties.value.deserializer','Кафка SSL: десериализатор входящих сообщений'),
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.binder.brokers','Кафка: список брокеров'),
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.default.consumer.enableDlq','Кафка: включить очередь ошибочных сообщений на все топики'),
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.default.consumer.topic.properties.retention.ms','Кафка: время до применения политики очистки брокера'),
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.default.producer.topic.properties.cleanup.policy','Кафка: политика очистки брокера'),
	 ('spring.cloud.stream.binders.kafka.environment.spring.cloud.stream.kafka.default.producer.topic.properties.retention.ms','Кафка: время до применения политики очистки брокера'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.producerProperties.key.serializer','Кафка SSL: сериализатор ключа исходящего сообщения'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.binder.producerProperties.value.serializer','Кафка SSL: сериализатор исходящего сообщения');
INSERT INTO configs.descriptions ("key",description) VALUES
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.default.consumer.enableDlq','Кафка SSL: включение очереди ошибочных сообщений на все топики'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.default.consumer.topic.properties.cleanup.policy','Кафка SSL: политика очистки топика'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.default.consumer.topic.properties.retention.ms','Кафка SSL: время до применения политики очистки топика'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.default.producer.topic.properties.cleanup.policy','Кафка SSL: политика очистки топика'),
	 ('spring.cloud.stream.binders.kafka-ssl.environment.spring.cloud.stream.kafka.default.producer.topic.properties.retention.ms','Кафка SSL: время до применения политики очистки топика'),
	 ('spring.cloud.stream.default-binder','Приложение: биндер по-умолчанию'),
	 ('spring.datasource.driver-class-name','Приложение: используемый драйвер базы данных'),
	 ('spring.profiles.include','Приложение: список используемых профилей'),
	 ('spring.security.oauth2.resourceserver.jwt.jwk-set-uri','Приложение: ссылка на jwks'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.configuration.ssl.enabled.protocols','Кафка с реестром схем: Список поддерживаемых протоколов для SSL');
INSERT INTO configs.descriptions ("key",description) VALUES
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.configuration.security.protocol','Кафка с реестром схем: Используемый кафкой протокол безопасности. Допустимые значения см. в документации кафки (PLAINTEXT, SSL, SASL_SSL и др.)'),
	 ('management.endpoints.web.exposure.include','Определение списка эндпоинтов, отображаемых актуатором'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.binder.auto-alter-topics','Кафка с реестром схем: разрешение на изменение топика'),
	 ('spring.cloud.stream.binders.kafka-avro.environment.spring.cloud.stream.kafka.default.producer.topic.properties.records.policy.enabled','Кафка с реестром схем: Настройка для server-side проверки соответствия сообщения схеме'),
	 ('spring.cloud.stream.binders.kafka.type','Кафка: тип биндера'),
	 ('spring.cloud.stream.binders.kafka-avro.type','Кафка с реестром: тип биндера'),
	 ('spring.cloud.stream.binders.kafka-ssl.type','Кафка SSL: тип биндера');