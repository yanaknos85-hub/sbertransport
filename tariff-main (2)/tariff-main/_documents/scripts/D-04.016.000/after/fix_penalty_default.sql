update tariff.contract set driver_late_pickup_penalty = 0.1 where driver_late_pickup_penalty = 0.01;
update tariff.contract set poor_service_quality_penalty = 0.1 where poor_service_quality_penalty = 0.01;
update tariff.contract set driver_order_cancellation_penalty = 0.1 where driver_order_cancellation_penalty = 0.01;