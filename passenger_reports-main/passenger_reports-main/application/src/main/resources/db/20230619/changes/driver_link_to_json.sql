update reports.request
set driver = jsonb_build_object(
        'id', d.id,
        'active', d.active,
        'autoparkId', d.autopark_id,
        'contactPhone', d.contact_phone,
        'contractorId', d.contractor_id,
        'driverLicenseNumber', d.driver_license_number,
        'experience', d.experience,
        'firstName', d.first_name,
        'lastName', d.last_name,
        'passport', d.passport,
        'patronymic', d.patronymic,
        'rating', d.rating,
        'serviceProviderLicenseNumber', d.service_provider_license_number,
        'licenseClasses', (select jsonb_agg(dlc.class) from reports.driver_license_classes dlc where dlc.driver_id = d.id),
        'tags', (select jsonb_agg(dt.id) from reports.driver_tag_connector dtg left join reports.driver_tag dt on dt.id = dtg.tag_id where dtg.driver_id =
                                                                                                                                   d.id)
    )
from reports.driver d where request.driver_id = d.id;