do $$
    declare
        batch_size   integer = 100000;
        rows_updated integer = 0;
    begin
        loop
            update telemechanic.ewb
            set communication_type = 'URBAN',
                transportation_type = 'OWN_ACCOUNT_TRANSPORTATION'
            where id in (
                select id
                from telemechanic.ewb
                where communication_type is null
                   or transportation_type is null
                limit batch_size
            );
            commit;

            get diagnostics rows_updated = row_count;
            if rows_updated = 0 then
                exit;
            end if;
        end loop;
    end $$;