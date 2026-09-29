update trips_cargo.driver
set consent = cd.consent
    from contractors.driver cd where cd.id = driver.id;

update trips_cargo.dispatcher
set consent = cd.consent
    from contractors.dispatcher cd where cd.id = dispatcher.id;