package ru.sber.transport.telemechanic.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.telemechanic.model.MedicalCheckUpModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class SecondTitleMapperTest {
    private final SecondTitleMapper mapper = Mappers.getMapper(SecondTitleMapper.class);
    
    @Test
    void mapWithExam() {
        var model = new MedicalCheckUpModel("data", "signature", "file-name", UUID.randomUUID(),
                                            new MedicalCheckUpModel.MedicInfo("Иванов Иван Иванович", "123456", "ООО Центр здоровья", "Поликлиника №1", "Врач-терапевт", "SIGN-12345", LocalDateTime.now().plusYears(1)),
                                            new MedicalCheckUpModel.ExamInfo(true, LocalDateTime.now(), LocalDateTime.now(), 120, 80, 70, BigDecimal.valueOf(36.6), BigDecimal.ZERO, ""),
                                            true);
        
        var mappedObject = mapper.medicalCheckUpWithExamToSecondTitleStraightResponse(model.name(), model.ewbUuid(), model.medicInfo(),
                                                                                      model.exam());
        
        assertThat(mappedObject.name()).isEqualTo("file-name");
        assertThat(mappedObject.ewbUuid()).isEqualTo(model.ewbUuid());
        assertThat(mappedObject.medicInfo().fio()).isEqualTo("Иванов Иван Иванович");
        assertThat(mappedObject.exam().medicRequestStatus()).isTrue();
    }
}
