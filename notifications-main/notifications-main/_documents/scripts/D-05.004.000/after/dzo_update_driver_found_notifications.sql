update notifications_settings.channel
set text = 'На заявку {ID} назначен автомобиль: {carInfo}. Водитель: {driver.driverFio}, {driver.phoneNumber}.'
where settings_id  in (select id from notifications_settings.notification where description = 'notice_4006' and "class" = 'REQUEST_GROUP_TRANSFER' and "type" = 'GROUP_TRANSFER_DRIVER_FOUND');

update notifications_settings.channel
set text = 'Уважаемый гость, ваш трансфер подтвержден
{localDesiredDate}, {localDesiredTime}
{carInfo}
{departureAddress} - {destinationAddress}
С вами свяжется водитель {driver.driverFio} ({driver.phoneNumber})
Мы позаботимся о вашем комфорте!'
where settings_id  in (select id from notifications_settings.notification where description = 'notice_4007' and "class" = 'REQUEST_GROUP_TRANSFER' and "type" = 'GROUP_TRANSFER_DRIVER_FOUND_MZK');

