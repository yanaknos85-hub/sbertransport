-- отключение всех настроек для отправки SMS
update notifications_settings.channel ch
set active = false
where ch.channel = 'SMS'