update trips.trips tt
set report_created = false
where tt.id not in (select tr.id from trips_reports.report tr) and tt.report_created = true;