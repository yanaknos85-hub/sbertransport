package ru.sberbank.ditsib.transport.constants;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Коды оплаты.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Schema(title = "Код вида оплаты")
public enum PaymentTypeCode {

    /**
     * С документами.
     */
    CODE_4666(TransportTypeEnum.PUBLIC, "Есть документальное подтверждение.", 4666),

    /**
     * Без документов.
     */
    CODE_4667(TransportTypeEnum.PUBLIC,"Нет документального подтверждения.", 4667),

    /**
     * Компенсация в рамках порога.
     */
    CODE_4661(TransportTypeEnum.PERSONAL,"Компенсация поездки не превышает пороговое значение.",
            PaymentLimitForEngineVolume.ENGINE_VOLUME_2000, 4661),

    /**
     * Компенсация сверх порога.
     */
    CODE_4665(TransportTypeEnum.PERSONAL,"Часть компенсации, сверх порогового значения.",  4665),

    /**
     * ТС в собственности третьих лиц.
     */
    CODE_4664(TransportTypeEnum.PERSONAL,"Транспорт находится собственности третьих лиц.", 4664);

    private final TransportTypeEnum transportType;

    private final String comment;

    private PaymentLimitForEngineVolume paymentLimit;

    private final int code;

    /**
     * Получение лимита оплаты для объема двигателя.
     *
     * @param engineVolume объем двигателя.
     *
     * @return лимит оплаты.
     */
    public Integer getPaymentLimitForEngineVolume(Integer engineVolume) {
        if (paymentLimit != null && engineVolume != null) {
            return paymentLimit.engineVolumeLimit >= engineVolume ? paymentLimit.paymentLimitForSmallerEngine : paymentLimit.paymentLimitForLargerEngine;
        }
        return Integer.MAX_VALUE;
    }

    private enum PaymentLimitForEngineVolume {
        ENGINE_VOLUME_2000(2000, 2400_00, 3000_00);

        private final Integer engineVolumeLimit;
        private final Integer paymentLimitForSmallerEngine;
        private final Integer paymentLimitForLargerEngine;

        PaymentLimitForEngineVolume(Integer engineVolumeLimit, Integer lessLimit, Integer moreLimit) {
            this.engineVolumeLimit = engineVolumeLimit;
            this.paymentLimitForSmallerEngine = lessLimit;
            this.paymentLimitForLargerEngine = moreLimit;
        }
    }

    /**
     * Получение типа оплаты по идентификатору.
     *
     * @param id идентификатор.
     *
     * @return тип оплаты.
     */
    public static PaymentTypeCode parse(Integer id) {
        if (id != null) {
            for (PaymentTypeCode item : PaymentTypeCode.values()) {
                if (Integer.valueOf(item.getCode()).equals(id)) {
                    return item;
                }
            }
        }
        return null;
    }
}
