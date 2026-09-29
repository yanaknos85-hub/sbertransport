package ru.sberbank.ditsib.transport.srm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.srm.config.SrmSettingNames;
import ru.sberbank.ditsib.transport.srm.model.SrmSetting;

/**
 * Limit settings repository
 */
@Repository
@Transactional(readOnly = true)
public interface SrmSettingRepository extends JpaRepository<SrmSetting, SrmSettingNames> {

}
