package ru.sber.transport.telemechanic.database.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;
import ru.sber.transport.telemechanic.enumerate.FileStatus;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "check_photo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckPhoto {
    
    /**
     * Идентификатор записи о фото
     */
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Идентификатор записи о проверке
     */
    @NotNull
    private UUID checkId;
    
    /**
     * Дата и время создания фото
     */
    @NotNull
    private LocalDateTime creationTime;
    
    /**
     * Статус загрузки файла в хранилище s3
     */
    @Enumerated(EnumType.STRING)
    private FileStatus fileStatus;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (CheckPhoto) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
