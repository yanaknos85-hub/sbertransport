do
$$
    declare
        drivers int;
        dispatchers int;
        contr contractors.contractor%rowtype;
    begin
        for contr in select * from contractors.contractor where active is true loop
                select count(id) into drivers from contractors.driver where contractor_id = contr.id and is_active is true;
                select count(id) into dispatchers from contractors.dispatcher where contractor_id = contr.id and active is true;
                update contractors.contractor set employee_count = drivers + dispatchers where id = contr.id;
            end loop;
    end;
$$
language plpgsql