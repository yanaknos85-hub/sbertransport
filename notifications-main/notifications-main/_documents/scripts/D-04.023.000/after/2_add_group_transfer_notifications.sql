DO $$
DECLARE
    v_settings_id uuid;
    v_text text;
    v_oganizationId uuid := :oganizationId::uuid;
BEGIN
    RAISE NOTICE'Starting script adding group transfer notifications ';

    v_text := 'Уважаемый гость, ваш трансфер подтвержден
{localDesiredDate}, {localDesiredTime}
{carInfo}
{departureAddress} - {destinationAddress}
С вами свяжется водитель {driverFio} ({driverPhone})
Мы позаботимся о вашем комфорте!';

    select notifications_settings.create_or_update_notification_with_send_time (
        p_class => 'REQUEST_GROUP_TRANSFER',
        p_name => 'Водитель назначен',
        p_description => 'notice_4007',
        p_parent_id => v_oganizationId,
        p_owner_id => null,
        p_type =>'GROUP_TRANSFER_DRIVER_FOUND_MZK',
        p_text => null,
        p_parent_type => 'ORGANIZATION',
        -- channels
        p_text_channel => v_text,
        p_push_active => false,
        p_sms_active => true,
        p_email_active => false,
        -- send time
        p_time_before => 3600000000000,
        p_type_send_time => 'BEFORE_DEADLINE',
        p_field_name => null,
        p_deadline_field_name => 'desiredDate'
        )
	 into v_settings_id;

	 --На согласовании	GROUP_TRANSFER_AWAITING_APPROVAL	✔	P/S/E	Только для согласующих  --notice_4002
    v_text := 'Вам поступила заявка {id} на согласование.';

    select notifications_settings.create_or_update_notification_with_send_time (
        p_class => 'REQUEST_GROUP_TRANSFER',
        p_name => 'На согласовании',
        p_description => 'notice_4002',
        p_parent_id => v_oganizationId,
        p_owner_id => null,
        p_type => 'GROUP_TRANSFER_AWAITING_APPROVAL',
        p_text => null,
        p_parent_type => 'ORGANIZATION',
        -- channels
        p_text_channel => v_text,
        p_push_active => true,
        p_sms_active => true,
        p_email_active => true,
        -- send time
        p_time_before => 0,
        p_type_send_time => 'AT_EVENT',
        p_field_name => null,
        p_deadline_field_name => null
        )
	 into v_settings_id;

     ---Согласовано	GROUP_TRANSFER_APPROVED	✔	P/S/E	Для инициатора GroupTransferApprovedCommand --notice_4003
    v_text := 'Ваша заявка {ID} согласована.';

    select notifications_settings.create_or_update_notification_with_send_time (
        p_class => 'REQUEST_GROUP_TRANSFER',
        p_name => 'Согласовано',
        p_description => 'notice_4003',
        p_parent_id => v_oganizationId,
        p_owner_id => null,
        p_type => 'GROUP_TRANSFER_APPROVED',
        p_text => null,
        p_parent_type => 'ORGANIZATION',
        -- channels
        p_text_channel => v_text,
        p_push_active => true,
        p_sms_active => true,
        p_email_active => true,
        -- send time
        p_time_before => 0,
        p_type_send_time => 'AT_EVENT',
        p_field_name => null,
        p_deadline_field_name => null
        )
	 into v_settings_id;

--4	Ожидайте назначение водителя	Пассажир / инициатор	P, S, E	Заявка передана контрагенту	«Ваша заявка {ID} получена для исполнения. В ближайшее время будет назначен автомобиль и водитель.»
    --Ожидайте назначение	GROUP_TRANSFER_AWAITING_SEARCH	✔	P/S/E	Для пассажира GroupTransferAwaitingSearchCommand --notice_4004
    v_text := 'Ваша заявка {ID} получена для исполнения. В ближайшее время будет назначен автомобиль и водитель.';

    select notifications_settings.create_or_update_notification_with_send_time (
        p_class => 'REQUEST_GROUP_TRANSFER',
        p_name => 'жидайте назначение водителя',
        p_description => 'notice_4004',
        p_parent_id => v_oganizationId,
        p_owner_id => null,
        p_type => 'GROUP_TRANSFER_AWAITING_SEARCH',
        p_text => null,
        p_parent_type => 'ORGANIZATION',
        -- channels
        p_text_channel => v_text,
        p_push_active => true,
        p_sms_active => true,
        p_email_active => true,
        -- send time
        p_time_before => 0,
        p_type_send_time => 'AT_EVENT',
        p_field_name => null,
        p_deadline_field_name => null
        )
	 into v_settings_id;

	 -- 6	Водитель назначен	Пассажир / инициатор	P, S, E	При получении данных о водителе и авто	«На заявку {ID} назначен автомобиль: {марка, модель, госномер}. Водитель: {ФИО, телефон}.»
     -- Водитель назначен	GROUP_TRANSFER_DRIVER_FOUND	✔	P/S/E	Системное уведомление GroupTransferDriverFoundCommand --notice_4006
    v_text := 'На заявку {ID} назначен автомобиль: {carInfo}. Водитель: {driverFio}, {driverPhone}.';
    select notifications_settings.create_or_update_notification_with_send_time (
        p_class => 'REQUEST_GROUP_TRANSFER',
        p_name => 'Водитель назначен',
        p_description => 'notice_4006',
        p_parent_id => v_oganizationId,
        p_owner_id => null,
        p_type => 'GROUP_TRANSFER_DRIVER_FOUND',
        p_text => null,
        p_parent_type => 'ORGANIZATION',
        -- channels
        p_text_channel => v_text,
        p_push_active => true,
        p_sms_active => true,
        p_email_active => true,
        -- send time
        p_time_before => 0,
        p_type_send_time => 'AT_EVENT',
        p_field_name => null,
        p_deadline_field_name => null
        )
	 into v_settings_id;
--8	Водитель ожидает	Пассажир / инициатор	P, S, E	Водитель прибыл в точку отправления	«Водитель ожидает вас в точке отправления. Авто: {марка, модель, госномер}.»
-- Водитель ожидает	GROUP_TRANSFER_DRIVER_ARRIVED	✔	P/S/E	Для пассажира и клиента GroupTransferDriverArrivedCommand --notice_4009
    v_text := 'Водитель прибыл в точку отправления	«Водитель ожидает вас в точке отправления. Авто: {carInfo}.';
    select notifications_settings.create_or_update_notification_with_send_time (
        p_class => 'REQUEST_GROUP_TRANSFER',
        p_name => 'Водитель ожидает',
        p_description => 'notice_4009',
        p_parent_id => v_oganizationId,
        p_owner_id => null,
        p_type => 'GROUP_TRANSFER_DRIVER_ARRIVED',
        p_text => null,
        p_parent_type => 'ORGANIZATION',
        -- channels
        p_text_channel => v_text,
        p_push_active => true,
        p_sms_active => true,
        p_email_active => true,
        -- send time
        p_time_before => 0,
        p_type_send_time => 'AT_EVENT',
        p_field_name => null,
        p_deadline_field_name => null
        )
	 into v_settings_id;

--	 10	Поездка завершена	Пассажир	P, E	Завершение поездки	«Ваша поездка завершена. Просим оценить сервис.»
--Поездка завершена	GROUP_TRANSFER_TRIP_FINISHED	✔	P/E	-   GroupTransferTripFinishedCommand --notice_4011
    v_text := 'Ваша поездка завершена. Просим оценить сервис.';
    select notifications_settings.create_or_update_notification_with_send_time (
        p_class => 'REQUEST_GROUP_TRANSFER',
        p_name => 'Поездка завершена',
        p_description => 'notice_4011',
        p_parent_id => v_oganizationId,
        p_owner_id => null,
        p_type => 'GROUP_TRANSFER_TRIP_FINISHED',
        p_text => null,
        p_parent_type => 'ORGANIZATION',
        -- channels
        p_text_channel => v_text,
        p_push_active => true,
        p_sms_active => false,
        p_email_active => true,
        -- send time
        p_time_before => 0,
        p_type_send_time => 'AT_EVENT',
        p_field_name => null,
        p_deadline_field_name => null
        )
	 into v_settings_id;

--    RAISE NOTICE'Для удаления настроек, выполните:';
--    RAISE NOTICE'delete from notifications.notification nn where  nn.settings_id = ''%''', v_settings_id;
--    RAISE NOTICE'delete from notifications_settings.channel where settings_id  = ''%''', v_settings_id;
--    RAISE NOTICE'delete from notifications_settings.send_time where settings_id = ''%''', v_settings_id;
--    RAISE NOTICE'delete from notifications_settings.send_count where settings_id = ''%''', v_settings_id;
--    RAISE NOTICE'delete from notifications_settings.notification where id = ''%''', v_settings_id;
END
$$;


