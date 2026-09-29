update trips.driver
set consent = cd.consent
    from contractors.driver cd where cd.id = driver.id;

update trips.dispatcher
set consent = cd.consent
    from contractors.dispatcher cd where cd.id = dispatcher.id;