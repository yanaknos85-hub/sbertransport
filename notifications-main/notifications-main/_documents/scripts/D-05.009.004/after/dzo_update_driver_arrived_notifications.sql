-- Обновление текста уведомления notice_4009 (GROUP_TRANSFER_DRIVER_ARRIVED)

UPDATE notifications_settings.channel
SET text = 'Водитель ожидает Вас в точке отправления по адресу {departureAddress}, автомобиль {carInfo}, тел {driver.phoneNumber}'
WHERE settings_id IN (
    SELECT id 
    FROM notifications_settings.notification 
    WHERE description = 'notice_4009' 
      AND "class" = 'REQUEST_GROUP_TRANSFER' 
      AND "type" = 'GROUP_TRANSFER_DRIVER_ARRIVED'
);
