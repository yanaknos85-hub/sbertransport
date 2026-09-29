alter table request.request_for_personal
    add column additional_sum bigint default null;
comment
on column request.request_for_personal.additional_sum is 'Дополнительная сумма для начисления в копейках';

alter table request.request_for_personal
    add column additional_sum_reason text default null;
comment
on column request.request_for_personal.additional_sum_reason is 'причина для начисления дополнительной суммы';