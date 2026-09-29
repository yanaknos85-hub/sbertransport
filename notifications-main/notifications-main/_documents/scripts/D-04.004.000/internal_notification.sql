update notifications_settings.channel
set "text" =
'Согласование поездки на такси {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.

Согласуйте заявку до истечения контрольного срока.

Сигма - {sigmaLink} (ПРОМ)

Альфа - {alphaLink} (ПРОМ)'
where id in (select nsc.id from notifications_settings.channel as nsc
join notifications_settings.notification as nsn
on nsn.id = nsc.settings_id
where nsn.description  = 'notice_101'
and nsc.channel = 'EMAIL');

update notifications_settings.channel
set "text" =
'Согласование поездки на общественном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.

Согласуйте заявку до истечения контрольного срока.

Сигма - {sigmaLink} (ПРОМ)

Альфа - {alphaLink} (ПРОМ)'
where id in (select nsc.id from notifications_settings.channel as nsc
join notifications_settings.notification as nsn
on nsn.id = nsc.settings_id
where nsn.description  = 'notice_201'
and nsc.channel = 'EMAIL');

update notifications_settings.channel
set "text" =
'Согласование поездки на личном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.

Согласуйте заявку до истечения контрольного срока.

Сигма - {sigmaLink} (ПРОМ)

Альфа - {alphaLink} (ПРОМ)'
where id in (select nsc.id from notifications_settings.channel as nsc
join notifications_settings.notification as nsn
on nsn.id = nsc.settings_id
where nsn.description  = 'notice_301'
and nsc.channel = 'EMAIL');

update notifications_settings.channel
set "text" =
'Согласование поездки на каршеринге {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.

Согласуйте заявку до истечения контрольного срока.

Сигма - {sigmaLink} (ПРОМ)

Альфа - {alphaLink} (ПРОМ)'
where id in (select nsc.id from notifications_settings.channel as nsc
join notifications_settings.notification as nsn
on nsn.id = nsc.settings_id
where nsn.description  = 'notice_403'
and nsc.channel = 'EMAIL');

update notifications_settings.channel
set "text" =
'Согласование поездки на велосипеде {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.

Согласуйте заявку до истечения контрольного срока.

Сигма - {sigmaLink} (ПРОМ)

Альфа - {alphaLink} (ПРОМ)'
where id in (select nsc.id from notifications_settings.channel as nsc
join notifications_settings.notification as nsn
on nsn.id = nsc.settings_id
where nsn.description  = 'notice_501'
and nsc.channel = 'EMAIL');

update notifications_settings.channel
set "text" =
'Согласование поездки на самокате {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.

Согласуйте заявку до истечения контрольного срока.

Сигма - {sigmaLink} (ПРОМ)

Альфа - {alphaLink} (ПРОМ)'
where id in (select nsc.id from notifications_settings.channel as nsc
join notifications_settings.notification as nsn
on nsn.id = nsc.settings_id
where nsn.description  = 'notice_601'
and nsc.channel = 'EMAIL');

update notifications_settings.channel
set "text" =
'Согласование лимита подразделения.

Согласуйте заявку до истечения контрольного срока.

Сигма - {sigmaLink} (ПРОМ)

Альфа - {alphaLink} (ПРОМ)'
where id in (select nsc.id from notifications_settings.channel as nsc
join notifications_settings.notification as nsn
on nsn.id = nsc.settings_id
where nsn.description  = 'notice_801'
and nsn.class = 'REQUEST_LIMIT_DEPARTMENT'
and nsc.channel = 'EMAIL');

update notifications_settings.channel
set "text" =
'На согласование поступила заявка на грузоперевозку {humanReadableId} от {authorFio}.

Согласуйте заявку до истечения контрольного срока.

Сигма - {sigmaLink} (ПРОМ)

Альфа - {alphaLink} (ПРОМ)'
where id in (select nsc.id from notifications_settings.channel as nsc
join notifications_settings.notification as nsn
on nsn.id = nsc.settings_id
where nsn.description like 'notice_801%'
and nsn.class = 'REQUEST_CARGO'
and nsc.channel = 'EMAIL');