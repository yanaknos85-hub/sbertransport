alter table tariff.tariff
    drop column contractor_max_diff_computed_distance_percent,
    drop column contractor_max_diff_fact_distance_percent,
    drop column contractor_max_diff_computed_cost_percent,
    drop column contractor_max_diff_contractor_cost_percent,
    drop column contractor_max_diff_computed_waiting_percent;
