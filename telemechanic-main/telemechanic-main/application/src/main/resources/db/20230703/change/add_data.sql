alter table telemechanic.check_photo
    drop column attempt;

update telemechanic.request_check_list set status = 'CHECK_LIST_DECLINE';