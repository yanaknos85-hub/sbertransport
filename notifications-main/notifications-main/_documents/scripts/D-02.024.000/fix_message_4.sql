update notifications_settings.channel
set text = 'Заказ выполнит {driver.lastName} {driver.firstName} {driver.patronymic}, автомобиль {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.stateNumber}.'
where text = 'Заказ выполнит {taxiTrip.driver.lastName} {taxiTrip.driver.firstName} {taxiTrip.driver.patronymic}, автомобиль - {taxiTrip.vehicle.color} {taxiTrip.vehicle.brand} {taxiTrip.vehicle.model} {taxiTrip.vehicle.registrationNumber}.';

update notifications_settings.channel
set text = 'Водитель {driver.lastname} {driver.firstname} {driver.patronymic} ожидает в пункте назначения, автомобиль {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.stateNumber}.'
where text = 'Водитель {driver.lastname} {driver.firstname} {driver.patronymic} ожидает в пункте назначения, автомобиль - {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.registrationNumber}.';

update notifications_settings.channel
set text = 'Заказ выполнит {driver.lastName} {driver.firstName} {driver.patronymic}, автомобиль {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.stateNumber}.'
where text = 'Заказ выполнит {driver.lastName} {driver.firstName} {driver.patronymic}, автомобиль - {vehicle.color} {vehicle.brand} {vehicle.model} {vehicle.registrationNumber}.';