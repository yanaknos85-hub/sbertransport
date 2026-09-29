-- Удаление первого дубликата. Необходимо выполнять до тех пор, пока селект снизу не перестанет возвращать результаты
delete from driver_track.route
where ctid in
(
	select rn."array_agg"[1]
	from(
		select r.trip_id, r."source", array_agg(ctid), count(*)
		from driver_track.route r
		group by r.trip_id, r."source"
		having count(*) >= 2
		) rn
);

-- Проверка на количество дубликатов
select r.trip_id, r."source", count(*), array_agg(ctid)
from driver_track.route r
group by r.trip_id, r."source"
having count(*) >= 2;