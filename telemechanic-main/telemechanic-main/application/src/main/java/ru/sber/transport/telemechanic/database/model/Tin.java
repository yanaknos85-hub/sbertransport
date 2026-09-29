package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "tin")
@NoArgsConstructor
@AllArgsConstructor
public class Tin {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @NotNull
    private UUID employeeId;
    
    @NotBlank
    @Size(max = 12)
    private String tin;
}
