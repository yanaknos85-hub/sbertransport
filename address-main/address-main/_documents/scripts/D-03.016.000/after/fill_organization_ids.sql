insert into addresses.employee (id, user_id, organization_id)
(select emp.id, coalesce(emp.user_id, emp.id), coalesce(emp.organization_id, (select dep.organization_id from corporate.department dep where dep.id = emp.department_id)) from corporate.employee emp)
on conflict do nothing;