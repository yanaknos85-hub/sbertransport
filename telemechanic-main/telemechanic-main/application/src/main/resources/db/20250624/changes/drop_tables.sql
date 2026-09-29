drop table telemechanic.medic_organization;
drop table telemechanic.technic_organization;
drop table telemechanic.medical_license;
delete from telemechanic.roles r where r.url_id in (select id from telemechanic.urls where url like '/medical-license/search/' and method like 'POST');
delete from telemechanic.urls where url like '/medical-license/search/' and method like 'POST';
delete from telemechanic.roles r where r.url_id in (select id from telemechanic.urls where url like '/medical-license/' and method like 'POST');
delete from telemechanic.urls where url like '/medical-license/' and method like 'POST';
delete from telemechanic.roles r where r.url_id in (select id from telemechanic.urls where url like '/medical-license/{id}/' and method like 'PATCH');
delete from telemechanic.urls where url like '/medical-license/{id}/' and method like 'PATCH'