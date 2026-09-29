insert into addresses.address (country, region, city, street, house, building, structure, id, latitude, longitude)
    (select country,
            region,
            city,
            street,
            house,
            building,
            structure,
            id,
            latitude,
            longitude
     from corporate.address) on conflict do nothing;

insert into addresses.favorite_address (id, address_id, "label", "owner")
    (select id, address_id, "label", employee_id from corporate.favorite_address) on conflict do nothing;

insert into addresses.frequently_address (id, "owner", address_id, usage_count, "first")
    (select id, employee_id, address_id, usage_count, "first" from corporate.frequently_address) on conflict do nothing;

insert into addresses.meeting_address (id, "label", address_id)
    (select id, "label", address_id from corporate.meeting_address) on conflict do nothing;
