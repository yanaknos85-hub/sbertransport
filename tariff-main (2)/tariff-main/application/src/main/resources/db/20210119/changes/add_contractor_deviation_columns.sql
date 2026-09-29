alter table tariff.tariff
    add column contractor_max_diff_computed_distance_percent  int4,
    add column contractor_max_diff_fact_distance_percent      int4,
    add column contractor_max_diff_computed_cost_percent      int4,
    add column contractor_max_diff_contractor_cost_percent    int4,
    add column contractor_max_diff_computed_waiting_percent   int4;


comment on column tariff.tariff.contractor_max_diff_computed_distance_percent is 'Допустимый % отклонения протяженности маршрута от контрагента и расчетной протяженности в АС';
comment on column tariff.tariff.contractor_max_diff_fact_distance_percent     is 'Допустимый % отклонения протяженности маршрута от контрагента и фактической протяженности в АС';
comment on column tariff.tariff.contractor_max_diff_computed_cost_percent     is 'Допустимый % отклонения стоимости поездки в реестре контрагента и расчетной стоимости в АС';
comment on column tariff.tariff.contractor_max_diff_contractor_cost_percent   is 'Допустимый % отклонения стоимости поездки в реестре контрагента и возвращенной стоимости от подрядчика';
comment on column tariff.tariff.contractor_max_diff_computed_waiting_percent  is 'Допустимый % отклонения времени ожидания в реестре контрагента и времени ожидания предварительно рассчитанного в АС';