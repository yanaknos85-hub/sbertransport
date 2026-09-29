DO $$
BEGIN
    RAISE NOTICE 'Started script deleting notifications';

	call notifications_settings.delete_unused_notification_settings(
	        p_class => 'REQUEST_PERSONAL',
            p_type =>'COOP_TRIP_ATTACHMENT_APPROVE'
	        );
	call notifications_settings.delete_unused_notification_settings(
	        p_class => 'REQUEST_PERSONAL',
            p_type =>'COOP_TRIP_ATTACHMENT_STATUS'
	        );
	call notifications_settings.delete_unused_notification_settings(
	        p_class => 'REQUEST_PERSONAL',
            p_type =>'ADDITIONAL_WAYPOINTS_APPROVAL'
	        );
	call notifications_settings.delete_unused_notification_settings(
	        p_class => 'REQUEST_PERSONAL',
            p_type =>'ADDITIONAL_WAYPOINTS_STATUS'
	        );

    RAISE NOTICE 'Finished script deleting notifications';
END
$$;

