DELETE FROM reports.request
WHERE passenger_id IN (SELECT id FROM reports.employee WHERE human_readable_id is NULL AND personnel_number is NULL) OR
      author_id IN (SELECT id FROM reports.employee WHERE human_readable_id is NULL AND personnel_number is NULL);