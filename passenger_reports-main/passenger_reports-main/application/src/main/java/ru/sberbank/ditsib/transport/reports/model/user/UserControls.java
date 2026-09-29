package ru.sberbank.ditsib.transport.reports.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Настройки пользователя - перечисление пользовательских элементов
 */
@Entity
@Table(schema = "reports", name = "user_controls")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserControls {

    /**
     * Первичный ключ
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Идентификатор таблицы user_preferences
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_preferences_id", referencedColumnName = "id")
    private UserPreferences userPreferencesId;

    /**
     * Тип элемента управления
     */
    @Column(name = "type_control")
    private String typeControl;

    /**
     * Наименование элемента управления
     */
    @Column
    private String value;

    /**
     * Настройки пользователя - перечисление свойств элементов
     */
    @OneToMany(mappedBy = "userControlsId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserProperty> userPropertyList = new ArrayList<>();

}
