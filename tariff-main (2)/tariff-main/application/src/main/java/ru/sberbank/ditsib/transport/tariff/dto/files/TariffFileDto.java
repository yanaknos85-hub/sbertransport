package ru.sberbank.ditsib.transport.tariff.dto.files;

/**
 * Интерфейс тарифов.
 */
public interface TariffFileDto {
    
    /**
     * Установить организацию.
     *
     * @param organization организация, которой принадлежит тариф.
     */
    void setOrganization(String organization);
    
    /**
     * Установить регион.
     *
     * @param region регион действия тарифа.
     */
    void setRegion(String region);
    
    /**
     * Установить идентификатор.
     *
     * @param id идентификатор тарифа.
     */
    void setId(String id);
    
    /**
     * Установить активность.
     *
     * @param active признак активности тарифа.
     */
    void setActive(boolean active);

    /**
     * Получить идентификатор.
     * @return идентификатор тарифа.
     */
    String getId();
}
