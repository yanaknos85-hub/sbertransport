package ru.sberbank.ditsib.transport.reports.model.user;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Настройки пользователя
 */
@Entity
@Table(schema = "reports", name = "user_preferences")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPreferences {

    /**
     * Первичный ключ
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Идентификатор пользователя
     */
    @Column(name = "user_id", unique = true)
    private UUID userId;

    /**
     * Наименование формы
     */
    @Column(name = "name_form")
    private String nameForm;

    /**
     * Настройки пользователя - перечисление пользовательских элементов
     */
    @OneToMany(mappedBy = "userPreferencesId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserControls> userControlsList = new ArrayList<>();
}
