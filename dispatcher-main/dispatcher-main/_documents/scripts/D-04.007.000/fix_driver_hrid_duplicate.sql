do
$$
    begin
        loop
            if (select count(*) from (select id, humanreadableid, contractor_id, row_number() over (partition by humanreadableid order by id) as rowNumber from dispatcher.driver) as duplicate where duplicate.rowNumber > 1) > 0 then
                update dispatcher.driver set humanreadableid = fix.correct_hrid from
                (select
                driverId,
                hrid,
                (select humanreadableid from dispatcher.driver where contractor_id = contr_id and humanreadableid like '%-0000%' order by humanreadableid desc limit 1) as max_hrid,
                concat_ws('-', 'DR', (regexp_split_to_array(hrid, '-'))[2],
                    lpad(((regexp_split_to_array((select humanreadableid from dispatcher.driver where contractor_id = contr_id and humanreadableid like '%-0000%' order by humanreadableid desc limit 1), '-'))[3]::int +1)::text, 8, '0')
                ) as correct_hrid
                from (
                        select
                        duplicate.id as driverId,
                        duplicate.humanreadableid as hrid,
                        duplicate.contractor_id as contr_id
                        from
                            (
                                select id, humanreadableid, contractor_id, row_number() over (partition by humanreadableid order by id) as rowNumber from dispatcher.driver
                            )
                        as duplicate where duplicate.rowNumber > 1
                     ) as selectForChange) as fix
                     where fix.driverId = dispatcher.driver.id;
            else
                exit;
            end if;
        end loop;
    end;
$$;
