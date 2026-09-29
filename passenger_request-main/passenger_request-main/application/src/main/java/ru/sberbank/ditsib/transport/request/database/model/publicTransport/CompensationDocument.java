package ru.sberbank.ditsib.transport.request.database.model.publicTransport;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "request", name = "compensation_document")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompensationDocument {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    //UUID генерится на фронте для создания папки / обращения к ней
    @NotNull
    @Column(name = "folder", nullable = false)
    private UUID folder;
    
    //<имя>.<формат>
    @NotBlank
    @Column(name = "name", nullable = false)
    
    private String fileName;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false)
    
    private UploadFileFormats fileFormat;
    
    @NotNull @Max(20971520)
    @Column(name = "size", nullable = false)
    
    private Integer fileSize;
    
    @NotNull
    @Column(name = "creation_time", nullable = false, updatable = false)
    
    private LocalDateTime creationTime;
}
