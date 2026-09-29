update notifications_corporate.employee
set phone = e.mobile_phone
from corporate.employee as e where e.id = notifications_corporate.employee.id;