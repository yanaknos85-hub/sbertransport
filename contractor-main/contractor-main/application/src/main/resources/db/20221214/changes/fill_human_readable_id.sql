insert into contractors.company_sq (id, prefix, orgdigitid, sq)
(select md5(random()::text || clock_timestamp()::text)::uuid as id, 'TR' as prefix, contractor.digit_id, count(trips.id) from contractors.trips inner join contractors.contractor on trips.contractor_id = contractor.id group by contractor.digit_id);

do
$$
    declare
        sq contractors.company_sq%rowtype;
        trip contractors.trips%rowtype;
        counter int8;
    BEGIN
        for sq in select * from contractors.company_sq where prefix = 'TR' loop
            counter=0;
            raise notice 'Update trips of %', sq.orgdigitid;
            for trip in select * from contractors.trips inner join contractors.contractor on trips.contractor_id = contractor.id where digit_id = sq.orgdigitid loop
                select counter+1 into counter;
                update contractors.trips
                set human_readable_id = 'TR-' || to_char(sq.orgdigitid, 'FM0000') || '-' || to_char(counter, 'FM00000000')
                where id = trip.id;
            end loop;
        end loop;
    end;
$$