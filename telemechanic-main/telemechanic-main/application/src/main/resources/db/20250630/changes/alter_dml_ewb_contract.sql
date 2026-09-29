DO
$do$
    declare
        rec_contracts record;
    BEGIN
        for rec_contracts in SELECT * from tariff_fleet.contract where id in (select id from tariff_fleet.ewb_contract)
            loop
                update telemechanic.ewb_contract ec
                set start = rec_contracts.start,
                    "end" = rec_contracts."end"
                where ec.contract_id = rec_contracts.id;
            end loop;
    END
$do$;