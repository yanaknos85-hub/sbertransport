package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.*;
import ru.sber.transport.telemechanic.dto.ewb.EwbInfo;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.*;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.SigningTelemechInfo;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechInformation;
import ru.sber.transport.telemechanic.helper.EwbHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true),
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = { LocalDate.class, LocalDateTime.class, DateTimeFormatter.class, EwbHelper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
public interface FifthTitleMapper {
    
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    DateTimeFormatter FULL_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy'T'HH:mm:ss+00:00");
    DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Mapping(target = "idFile", ignore = true)
    @Mapping(target = "versionProgram", constant = "1.0.0")
    @Mapping(target = "versionForm", constant = "5.01")
    @Mapping(target = "document", expression = "java(ewbInfoToFifthTitleDocument(source))")
    FifthTitleFile ewbInfoToFifthTitleFile(EwbInfo source);
    
    @Mapping(target = "knd", constant = "1110384")
    @Mapping(target = "informationDate", expression = "java(LocalDate.now().format(DATE_FORMATTER))")
    @Mapping(target = "informationTime", expression = "java(LocalDateTime.now().format(TIME_FORMATTER))")
    @Mapping(target = "fourthTitleInformation", expression = "java(ewbInfoToIdentificationFourthTitle(source))")
    @Mapping(target = "fifthTitleInformation", expression = "java(ewbInfoToFifthTitleInformation(source))")
    @Mapping(target = "signingTelemechInfo", expression = "java(ewbInfoToSigningTelemechInfo(source))")
    FifthTitleDocument ewbInfoToFifthTitleDocument(EwbInfo source);
    
    @Mapping(target = "fileName", source = "title.fileName")
    @Mapping(target = "createDate", expression = "java(source.title().getCreatedAt().format(DATE_FORMATTER))")
    @Mapping(target = "createTime", expression = "java(source.title().getCreatedAt().format(TIME_FORMATTER))")
    @Mapping(target = "sign", source = "signature")
    IdentificationFourthTitle ewbInfoToIdentificationFourthTitle(EwbInfo source);
    
    @Mapping(target = "ewbUuid", source = "ewb.ewbUuid")
    @Mapping(target = "isRouteFinish", constant = "1")
    @Mapping(target = "odometerInInformation", expression = "java(ewbInfoToOdometerInInformation(source))")
    @Mapping(target = "telemechInformation", expression = "java(ewbInfoToTelemechInformation(source))")
    FifthTitleInformation ewbInfoToFifthTitleInformation(EwbInfo source);
    
    @Mapping(target = "signType", constant = "1")
    @Mapping(target = "signConfirmationMethod", constant = "1")
    @Mapping(target = "fullName", source = "ewb.telemechIn")
    SigningTelemechInfo ewbInfoToSigningTelemechInfo(EwbInfo source);
    
    @Mapping(target = "finishRouteDateTime", expression = "java(source.ewb().getTelemechDecisionIn().format(FULL_TIME_FORMATTER))")
    @Mapping(target = "finishRouteTimeUtc", constant = "1")
    @Mapping(target = "odometerValue", source = "ewb.odometerIn")
    OdometerInInformation ewbInfoToOdometerInInformation(EwbInfo source);
    
    @Mapping(target = "fullName", source = "ewb.telemechIn")
    TelemechInformation ewbInfoToTelemechInformation(EwbInfo source);
}
