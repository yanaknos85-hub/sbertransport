update vehicle.transport
    set inventory_number = 0
    where inventory_number is null;

alter table vehicle.transport
    alter column inventory_number set not null;