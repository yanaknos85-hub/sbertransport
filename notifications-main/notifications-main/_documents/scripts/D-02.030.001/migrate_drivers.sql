insert into notifications_request.driver (id,
                                             contractor_id,
                                             first_name,
                                             last_name,
                                             patronymic,
                                             autopark_id,
                                             rating,
                                             phone,
                                             email) select id,
                                                           contractor_id,
                                                           first_name,
                                                           last_name,
                                                           patronymic,
                                                           null,
                                                           rating,
                                                           (select replace((select replace(contact_phone_number, '(', '')), ')', '')),
                                                           email from contractors.driver
on conflict do nothing;

update notifications_request.driver set contractor_id = d.contractor_id,
                                                           first_name = d.first_name,
                                                           last_name = d.last_name,
                                                           patronymic = d.patronymic,
                                                           autopark_id = null,
                                                           rating = d.rating,
                                                           phone = (select replace((select replace(d.contact_phone_number, '(', '')), ')', '')),
                                                           email = d.email from contractors.driver as d where notifications_request.driver.id = d.id;

