insert into dispatcher.vehicle (id,
	state_number,
	vin,
	body_type,
	color,
	manufacture_year,
	max_allowed_weight,
	mileage,
	transmission_type,
	autopark_id,
	engine_type,
	active,
	in_exploitation,
	model_name,
	model_brand,
	model_year,
	vehicle_type,
	transport_id) select
					md5(random()::text || clock_timestamp()::text)::uuid,
					vt.state_number,
					vt.vin_code as vin,
					vbt.title as body_type,
					vt.body_color as color,
					vt."year" as manufacture_year,
					vv.max_weight as max_allowed_weight,
					vt.current_mileage as mileage,
					vtt.title as transmission_type,
					da.id as autopark_id,
					vet.title as engine_type,
					(vt."status" = 'IN_USE') as active,
					(vt.exploitation_start is not null and vt.exploitation_end is null) as in_exploitation,
					vt.model_by_passport as model_name,
					vt.brand_by_passport as model_brand,
					vt."year" as model_year,
					'PASSENGER' as vehicle_type,
					vt.id as transport_id
					from vehicle.transport vt
					join vehicle.vehicle vv on vt.vehicle_id = vv.id
					join vehicle.engine_type vet on vv.engine_type_id = vet.id
					join vehicle.transmission_type vtt on vv.transmission_type_id = vtt.id
					join vehicle.body_type vbt on vv.body_type_id = vbt.id
					join vehicle.transport_department vtd on vt.id = vtd.transport_id
					join dispatcher.autopark da on da.routing_id = vtd.department_id
	on conflict (transport_id) do nothing;