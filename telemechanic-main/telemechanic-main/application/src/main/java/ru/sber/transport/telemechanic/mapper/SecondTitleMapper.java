package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.telemechanic.dto.SecondTitleStraightResponse;
import ru.sber.transport.telemechanic.model.MedicalCheckUpModel;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface SecondTitleMapper {
    
    SecondTitleStraightResponse medicalCheckUpWithExamToSecondTitleStraightResponse(String name, UUID ewbUuid,
                                                                                    MedicalCheckUpModel.MedicInfo medicInfo,
                                                                                    MedicalCheckUpModel.ExamInfo exam);
    
    @Mapping(target = "exam", ignore = true)
    SecondTitleStraightResponse medicalCheckUpWithoutExamToSecondTitleStraightResponse(
            String name, UUID ewbUuid, MedicalCheckUpModel.MedicInfo medicInfo);
}
