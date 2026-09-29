update authentication.role
set default_for = json_build_array('EMPLOYEE')
where "default" is true;
update authentication.role
set default_for = json_build_array()
where "default" is false;