update authentication.account
set scope = 'EMPLOYEE'
from corporate.employee as empl
where account.id = empl.id;

update authentication.account
set scope = 'DRIVER'
from contractors.driver as driv
where account.id = driv.id;

update authentication.account
set scope = 'DISPATCHER'
from contractors.dispatcher as disp
where account.id = disp.id;