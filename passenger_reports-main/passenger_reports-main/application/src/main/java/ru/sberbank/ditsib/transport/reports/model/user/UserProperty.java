package ru.sberbank.ditsib.transport.reports.model.user;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Настройки пользователя - перечисление свойств элементов
 */
@Entity
@Table(schema = "reports", name = "user_property")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProperty {

    /**
     * Первичный ключ
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Идентификатор таблицы user_controls
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_controls_id", referencedColumnName = "id")
    private UserControls userControlsId;

    /**
     * Наименование свойства
     */
    @Column(name = "name_setting")
    private String nameSetting;

    /**
     * Значение свойства элемента управления
     */
    @Column(name = "value_setting")
    private String valueSetting;

    /**
     * Порядок сортировки
     */
    @Column
    private Integer sort;
}
