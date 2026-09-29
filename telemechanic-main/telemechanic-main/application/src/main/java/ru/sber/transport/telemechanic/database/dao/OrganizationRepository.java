package ru.sber.transport.telemechanic.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.dto.OrganizationDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Repository of organizations
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    
    List<Organization> findAllByIdInOrderByOfficialName(Set<UUID> uuids);
    
    /**
     * Получаем первый контакт организации типа Phone, или пустую строку
     *
     * @param id Идентификатор записи об организации
     * @return контакт типа Phone
     */
    @Query(value = """
                   select tc.value
                   from telemechanic.contact tc
                            join telemechanic.organization_contact oc on tc.id = oc.contact_id
                            join telemechanic.organization o on oc.organization_id = o.id
                   where tc.type = 'PHONE'
                     and o.id = :id
                   limit 1
                   """,
           nativeQuery = true)
    String getFirstContactPhone(UUID id);
    
    /**
     * Получаем все активные организации, отсортированные по official name по возрастанию
     *
     * @return {@link List<Organization>}
     */
    @Query(value = """
           select o.id, o.official_name
           from telemechanic.organization o
           where o.active is true
           order by o.official_name
           """,
           nativeQuery = true)
    List<OrganizationDto> findAllActiveOrderByOfficialName();
    
    /**
     * Получаем все активные организации с внетренним контрактором, отсортированные по official name по возрастанию
     *
     * @return {@link List<Organization>}
     */
    @Query(value = """
           select o.id, o.official_name
           from telemechanic.organization o
           where o.active is true
                and o.contractor_external_id is not null
           order by o.official_name
           """,
           nativeQuery = true)
    List<OrganizationDto> findAllActiveWithInternalContractorOrderByOfficialName();
}
