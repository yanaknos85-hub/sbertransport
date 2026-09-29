package ru.sber.transport.address.providers.common_address.client;

import lombok.NonNull;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sber.transport.address.business.model.GeoAddress;

import java.math.BigDecimal;
import java.util.List;

/**
 * Клиент гео-провайдера.
 */
public interface GeoClient {

    /**
     * Получение адресов.
     *
     * @param search                       поисковая строка.
     * @param viewportTopLeftLatitude      верхняя левая широта просматриваемой области.
     * @param viewportTopLeftLongitude     верхняя левая долгота просматриваемой области.
     * @param viewportBottomRightLatitude  нижняя правая широта просматриваемой области.
     * @param viewportBottomRightLongitude нижняя правая долгота просматриваемой области.
     * @return список адресов.
     */
    List<GeoAddress> getAddresses(@NonNull @RequestParam("location") String search,
                                  @RequestParam("viewport_top_left_latitude") BigDecimal viewportTopLeftLatitude,
                                  @RequestParam("viewport_top_left_longitude") BigDecimal viewportTopLeftLongitude,
                                  @RequestParam("viewport_bottom_right_latitude") BigDecimal viewportBottomRightLatitude,
                                  @RequestParam("viewport_bottom_right_longitude") BigDecimal viewportBottomRightLongitude);


    /**
     * Получение адресов.
     *
     * @param latitude  широта.
     * @param longitude долгота.
     * @return список адресов.
     */
    List<GeoAddress> getAddress(@NonNull @RequestParam("latitude") BigDecimal latitude,
                             @NonNull @RequestParam("longitude") BigDecimal longitude);
}
