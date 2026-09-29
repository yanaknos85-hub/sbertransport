update notifications_settings.channel
set text = 'Согласование поездки на такси {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_101');

update notifications_settings.channel
set text = 'По вашей поездке {humanReadableId} завершен этап "Согласование" со статусом "{approveStatusDescription}".'
where settings_id in (select id from notifications_settings.notification where description = 'notice_102');

update notifications_settings.channel
set text = 'Согласование поездки на общественном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_201');

update notifications_settings.channel
set text = 'По вашей поездке {humanReadableId} завершен этап "Согласование" со статусом "{approveStatusDescription}".'
where settings_id in (select id from notifications_settings.notification where description = 'notice_202');

update notifications_settings.channel
set text = 'Необходимо подтвердить расходы на поездку по заявке {humanReadableId}.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_203');

update notifications_settings.channel
set text = 'Утверждение поездки на общественном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_204');

update notifications_settings.channel
set text = 'По вашей поездке {humanReadableId} завершен этап "Утверждение" со статусом "{requestStatusDescription}".'
where settings_id in (select id from notifications_settings.notification where description = 'notice_205');

update notifications_settings.channel
set text = 'По вашей поездке {humanReadableId} завершен этап "Ожидание выплаты" со статусом "{requestStatusDescription}".'
where settings_id in (select id from notifications_settings.notification where description = 'notice_206');

update notifications_settings.channel
set text = 'Согласование поездки на личном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_301');

update notifications_settings.channel
set text = 'По вашей поездке {humanReadableId} завершен этап "Согласование" со статусом "{approveStatusDescription}".'
where settings_id in (select id from notifications_settings.notification where description = 'notice_302');

update notifications_settings.channel
set text = 'По вашей поездке {humanReadableId} завершен этап "Согласование присоединения" со статусом "{requestStatusDescription}".'
where settings_id in (select id from notifications_settings.notification where description = 'notice_304');

update notifications_settings.channel
set text = 'Утверждение поездки на личном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_308');

update notifications_settings.channel
set text = 'По вашей поездке {humanReadableId} завершен этап "Утверждение маршрута" со статусом "{requestStatusDescription}".'
where settings_id in (select id from notifications_settings.notification where description = 'notice_309');

update notifications_settings.channel
set text = 'Ваша заявка {humanReadableId} была отправлена на выплату.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_310');

update notifications_settings.channel
set text = 'По вашей поездке {humanReadableId} завершен этап "Ожидание выплаты" со статусом "{requestStatusDescription}".'
where settings_id in (select id from notifications_settings.notification where description = 'notice_311');

update notifications_settings.channel
set text = 'Необходимо согласовать дополнительные точки в маршруте поездки на личном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_314');

update notifications_settings.channel
set text = 'Добавление дополнительных точек по вашей поездке {request.humanReadableId} было согласовано.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_315');

update notifications_settings.channel
set text = 'Добавление дополнительных точек по вашей поездке {request.humanReadableId} было отклонено.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_316');

update notifications_settings.channel
set text = 'Поступила новая заявка на подключение к корп. каршерингу {request.humanReadableId}. Обработайте заявку до истечения контрольного срока.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_401');

update notifications_settings.channel
set text = 'Согласование поездки на каршеринге {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_403');

update notifications_settings.channel
set text = 'По вашей поездке {humanReadableId} завершен этап "Согласование" со статусом "{approveStatusDescription}".'
where settings_id in (select id from notifications_settings.notification where description = 'notice_404');

update notifications_settings.channel
set text = 'Согласование поездки на велосипеде {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_501');

update notifications_settings.channel
set text = 'По вашей поездке {humanReadableId} завершен этап "Согласование" со статусом {approveStatusDescription}.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_502');

update notifications_settings.channel
set text = 'Согласование поездки на самокате {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_601');

update notifications_settings.channel
set text = 'По вашей поездке {humanReadableId} завершен этап "Согласование" со статусом "{approveStatusDescription}".'
where settings_id in (select id from notifications_settings.notification where description = 'notice_602');

update notifications_settings.channel
set text = 'Согласование лимита подразделения. Согласуйте заявку до истечения контрольного срока.'
where settings_id in (select id from notifications_settings.notification where description = 'notice_801');