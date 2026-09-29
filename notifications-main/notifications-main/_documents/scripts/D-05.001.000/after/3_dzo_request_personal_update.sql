DO $$
DECLARE
    v_settings_id notifications_settings.notification.id%TYPE;
BEGIN
    RAISE NOTICE 'Starting script for update notifications';

    call notifications_settings.update_all_notification_and_channels (
        p_class => 'REQUEST_PERSONAL',
        p_type =>'TRIP_START_REMIND',
        p_name => 'Напоминание о начале поездки',
        p_description => 'notice_305',
        p_text => null,
        -- channels
        p_text_channel => 'Ваша поездка скоро начнется. Не забудьте отметить локации в приложении.',
        p_push_active => true,
        p_sms_active => false,
        p_email_active => false
        );

    call notifications_settings.update_send_time_for (
        p_class => 'REQUEST_PERSONAL',
        p_type =>'TRIP_START_REMIND',
        p_time_before => 900000000000,
        p_type_send_time => 'BEFORE_DEADLINE',
        p_field_name => null,
        p_deadline_field_name => 'desiredDate'
        );

    call notifications_settings.update_all_notification_and_channels (
            p_class => 'REQUEST_PERSONAL',
            p_type =>'WAYPOINT_ARRIVED',
            p_name => 'Прибытие в промежуточный пункт на личном транспорте',
            p_description => 'notice_306',
            p_text => null,
            -- channels
            p_text_channel => 'Вы прибыли в промежуточный пункт, не забудьте отметить чек-бокс геопозиции в вашей заявке для ускорения выплаты компенсации.',
            p_push_active => true,
            p_sms_active => false,
            p_email_active => false
            );

    call notifications_settings.update_all_notification_and_channels (
            p_class => 'REQUEST_PERSONAL',
            p_type =>'TRIP_FINISHED',
            p_name => 'Поездка на личном транспорте завершена',
            p_description => 'notice_307',
            p_text => null,
            -- channels
            p_text_channel => 'Поездка успешно завершена',
            p_push_active => true,
            p_sms_active => false,
            p_email_active => false
            );

    call notifications_settings.update_all_notification_and_channels (
            p_class => 'REQUEST_PERSONAL',
            p_type =>'TRIP_AFFIRM',
            p_name => 'Утверждение финального маршрута на личном транспорте',
            p_description => 'notice_308',
            p_text => null,
            -- channels
            p_text_channel => 'Утверждение поездки на личном транспорте {request.passenger.lastName} {request.passenger.firstName} {request.passenger.patronymic}.',
            p_push_active => true,
            p_sms_active => true,
            p_email_active => true
            );

    call notifications_settings.update_all_notification_and_channels (
            p_class => 'REQUEST_PERSONAL',
            p_type =>'TRIP_AFFIRM_STATUS',
            p_name => 'Статус утверждения финального маршрута на личном транспорте',
            p_description => 'notice_309',
            p_text => null,
            -- channels
            p_text_channel => 'По вашей поездке {humanReadableId} завершен этап "Утверждение маршрута" со статусом "{requestStatusDescription}".',
            p_push_active => true,
            p_sms_active => false,
            p_email_active => true
            );

    call notifications_settings.update_all_notification_and_channels (
            p_class => 'REQUEST_PERSONAL',
            p_type =>'PAYMENT',
            p_name => 'Ожидание выплаты за поездку на личном транспорте',
            p_description => 'notice_310',
            p_text => null,
            -- channels
            p_text_channel => 'Ваша заявка {humanReadableId} была отправлена на выплату.',
            p_push_active => true,
            p_sms_active => false,
            p_email_active => true
            );


    call notifications_settings.update_all_notification_and_channels (
            p_class => 'REQUEST_PERSONAL',
            p_type =>'PAYMENT_STATUS',
            p_name => 'Статус ожидания выплаты за поездку на личном транспорте',
            p_description => 'notice_311',
            p_text => null,
            -- channels
            p_text_channel => 'По вашей поездке {humanReadableId} завершен этап "Ожидание выплаты" со статусом "{requestStatusDescription}".',
            p_push_active => true,
            p_sms_active => false,
            p_email_active => true
            );

    call notifications_settings.update_all_notification_and_channels (
            p_class => 'REQUEST_PERSONAL',
            p_type =>'COOP_TRIP_ATTACHMENT',
            p_name => 'Присоединение к совместной поездке на личном транспорте',
            p_description => 'notice_312',
            p_text => null,
            -- channels
            p_text_channel => 'К вашей поездке присоединился {passenger.lastName} {passenger.firstName} {passenger.patronymic}.',
            p_push_active => true,
            p_sms_active => true,
            p_email_active => true
            );

call notifications_settings.update_all_notification_and_channels (
            p_class => 'REQUEST_PERSONAL',
            p_type =>'COOP_TRIP_CHANGES',
            p_name => 'Изменение в совместной поездке на личном транспорте',
            p_description => 'notice_313',
            p_text => null,
            -- channels
            p_text_channel => 'Пассажир {passenger.lastName} {passenger.firstName} {passenger.patronymic} отказался от совместной поездки с вами № {magentaSharedRequest.magentaId}.',
            p_push_active => true,
            p_sms_active => true,
            p_email_active => true
            );

END
$$;

