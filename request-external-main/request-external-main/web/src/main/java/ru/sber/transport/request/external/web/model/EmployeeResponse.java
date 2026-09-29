package ru.sber.transport.request.external.web.model;

import ru.sber.transport.web.model.Employee;

/**
 * Ответ с данными о сотруднике.
 */
class EmployeeResponse extends Employee {

    /**
     * Конструктор для создания ответа с данными о сотруднике.
     *
     * @param passenger данные о сотруднике
     */
    public EmployeeResponse(ru.sber.transport.request.external.model.Employee passenger) {
        setId(passenger.getId());
        setLastName(passenger.getLastName());
        setFirstName(passenger.getFirstName());
        setPatronymic(passenger.getPatronymic());
    }

}
