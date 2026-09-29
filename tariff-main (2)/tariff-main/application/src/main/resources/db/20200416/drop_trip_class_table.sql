alter TABLE tariff.tariff_taxi
    drop column trip_class_id,
    add taxi_class varchar(255) not null;