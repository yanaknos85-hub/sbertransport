package ru.sberbank.ditsib.transport.request.service;


import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.LimitReservationResultDto;

/**
 * Feign client for limits application
 */
public interface ReservationService {
    
    /**
     * Резервирование средств по заявке. Для резервирования передается полная стоимость поездки (без учета бонусов) и количество бонусов (если есть),
     * которые необходимо списать
     *
     * @param request запрос
     * @param employee пользователь
     * @param sum стоимость поездки (полная сумма,
     * @param bonusSum бонусы
     *
     * @return dto
     */
    LimitReservationResultDto makeReservation(Request request, Employee employee, double sum, Long bonusSum);
    
    /**
     * Резервирование средств по заявке. Для резервирования передается полная стоимость поездки (без учета бонусов) и количество бонусов (если есть),
     * которые необходимо списать
     *
     * @param request запрос
     * @param transportType тип транспорта
     * @param employee пользователь
     * @param sum стоимость поездки (полная сумма,
     * @param bonusSum бонусы
     *
     * @return dto
     */
    LimitReservationResultDto makeReservation(
            Request request,
            TransportTypeEnum transportType,
            Employee employee,
            double sum,
            Long bonusSum
                                             );
    
    /**
     * Подтверждение списание средств с лимита
     *
     * @param request запрос
     * @param sumSpent фактическая сумма списания
     * @param transportTypeEnum тип транспорта
     */
    default void spend(Request request, Integer sumSpent, TransportTypeEnum transportTypeEnum) {
        spend(request, sumSpent, false, false, transportTypeEnum, null);
    }
    
    /**
     * Подтверждение списание средств с лимита
     *
     * @param request запрос
     * @param sumSpent фактическая сумма списания
     * @param isCoop флаг совместной поездки
     * @param isDriver флаг водителя
     * @param transportTypeEnum тип транспорта
     * @param moneySaved количество сэкономленных средств (совместная поездка)
     */
    void spend(Request request, Integer sumSpent, boolean isCoop, boolean isDriver, TransportTypeEnum transportTypeEnum, Integer moneySaved);
    
    void cancel(Request request);
    
}
