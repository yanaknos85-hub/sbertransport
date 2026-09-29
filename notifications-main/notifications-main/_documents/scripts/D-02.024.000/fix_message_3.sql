update notifications_settings.channel
set text = 'Согласование поездки на личном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where text = 'Согласование поездки на личном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic} {request.desiredDate}. Согласуйте заявку до {deadlineDateTime}';

update notifications_settings.channel
set text = 'Согласование поездки на каршеринге {passenger.lastName} {passenger.firstName} {passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where text = 'Согласование поездки на каршеринге {passenger.lastName} {passenger.firstName} {passenger.patronymic} {desiredDate}. Согласуйте заявку до {deadlineDateTime}';

update notifications_settings.channel
set text = 'Согласование поездки на самокате {passenger.lastName} {passenger.firstName} {passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where text = 'Согласование поездки на самокате {passenger.lastName} {passenger.firstName} {passenger.patronymic} {desiredDateStringValue}. Согласуйте заявку до {deadlineDateTime}';

update notifications_settings.channel
set text = 'Согласование поездки на велосипеде {passenger.lastName} {passenger.firstName} {passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where text = 'Согласование поездки на велосипеде {passenger.lastName} {passenger.firstName} {passenger.patronymic} {desiredDate}. Согласуйте заявку до {deadlineDateTime}';

update notifications_settings.channel
set text = 'Согласование поездки на каршеринге {passenger.lastName} {passenger.firstName} {passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where text = 'Согласование поездки на каршеринге {passenger.lastName} {passenger.firstName} {passenger.patronymic} {desiredDateStringValue}. Согласуйте заявку до {deadlineDateTime}';

update notifications_settings.channel
set text = 'Согласование поездки на самокате {passenger.lastName} {passenger.firstName} {passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where text = 'Согласование поездки на самокате {passenger.lastName} {passenger.firstName} {passenger.patronymic} {desiredDate}. Согласуйте заявку до {deadlineDateTime}';

update notifications_settings.channel
set text = 'Согласование поездки {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where text = 'Вам поступила новая заявка на согласование. Откройте раздел "Согласования" в приложении Сбертранспорт.';

update notifications_settings.channel
set text = 'Согласование поездки на велосипеде {passenger.lastName} {passenger.firstName} {passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where text = 'Согласование поездки на велосипеде {passenger.lastName} {passenger.firstName} {passenger.patronymic} {desiredDateStringValue}. Согласуйте заявку до {deadlineDateTime}';

update notifications_settings.channel
set text = 'Согласование поездки на такси {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where text = 'Согласование поездки на такси {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic} {request.desiredDate}. Согласуйте заявку до {deadlineDateTime}';

update notifications_settings.channel
set text = 'Согласование поездки на общественном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}. Согласуйте заявку до истечения контрольного срока.'
where text = 'Согласование поездки на общественном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic} {request.desiredDate}. Согласуйте заявку до {deadlineDateTime}';
