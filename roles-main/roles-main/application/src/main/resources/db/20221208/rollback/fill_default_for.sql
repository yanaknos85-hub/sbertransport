update roles.role
set "default" = true
where default_for::text ilike '%"EMPLOYEE"%';