update contractors.contractor
set contractor_type = 'API',
	service_type = 'EMPLOYEE_TRANSPORTATION'
where integration_type = 'JSON_API_1_0' and contractor_type is null and service_type is null

