package ru.sber.transport.telemechanic.service;

import org.springframework.data.domain.Page;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.database.model.Ewb;
import ru.sber.transport.telemechanic.dto.CheckResponse;
import ru.sber.transport.telemechanic.dto.DeclinedTelemechRequest;
import ru.sber.transport.telemechanic.dto.SecondTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.*;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.FifthTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.FifthTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.first_title.FirstTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchAllOrganizationsRequestDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchResponseDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchSelfOrganizationRequestDto;
import ru.sber.transport.telemechanic.dto.ewb.second_title.SecondTitleForm;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.SendAndSaveTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechOutTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechOutTitleSendResponse;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechOutTitlesRequest;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryAllOrganizationsRequest;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryResponse;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistrySelfOrganizationRequest;
import ru.sber.transport.telemechanic.model.MedicalCheckUpModel;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EwbService {
    
    TokenDto auth();
    
    UuidDto getUUID();
    
    FirstTitleResponse generateFirstTitle(FirstTitleRequest request, UUID userId);
    
    SecondTitleResponse generateSecondTitle(SecondTitleForm title, UUID userId);
    
    TelemechOutTitleResponse generateTelemechOutTitles(EwbTitleType titleType, TelemechOutTitlesRequest request, UUID userId);
    
    FifthTitleResponse generateFifthTitle(FifthTitleRequest request, UUID userId);
    
    Page<EwbSearchResponseDto> searchSelfOrganization(EwbSearchSelfOrganizationRequestDto request, UUID userId);
    
    Page<EwbSearchResponseDto> searchAllOrganizations(EwbSearchAllOrganizationsRequestDto request);
    
    Page<EwbSearchResponseDto> search(EwbSearchDto request);
    
    GetEwbDto getEwb(UUID id);
    
    Ewb getEwbByRequestId(UUID requestId);
    
    GetEwbRequestDto getEwbRequest(UUID userId);
    
    /**
     * Метод для отправки титула в Корус
     *
     * @param request данные о титуле, ЭПЛ и подпись
     * @param userId Ид пользователя
     */
    void sendAndSaveFirstTitle(FirstTitleDto request, UUID userId);
    
    void sendAndSaveSecondTitle(SendAndSaveTitleRequest request, UUID userId);
    
    TelemechOutTitleSendResponse sendAndSaveTelemechOutTitle(
            SendAndSaveTitleRequest request,
            EwbTitleType titleType,
            UUID userId
                                                            );
    
    void sendAndSaveFifthTitle(SendAndSaveTitleRequest request, UUID userId);
    
    /**
     * Метод для закрытия ЭПЛ
     *
     * @param request данные о заявке телемеханника и показания одометра
     * @param userId пользователь инициировавший закрытие ЭПЛ
     */
    void closeEwb(EwbCloseRequest request, UUID userId);
    
    /**
     * Метод для внесения показаний одометра при выходе на линию
     *
     * @param request данные о заявке телемеханника и показания одометра
     * @param userId пользователь предоставивший показания
     */
    CheckResponse addOdometerValue(OdometerValue request, UUID userId);
    
    void declineEwb(UUID requestId, DeclinedTelemechRequest request, UUID userId);
    
    GetQrCodeResponse getQrCode(UUID ewbId, UUID userId);
    
    Page<EwbRegistryResponse> searchRegistryForAllOrganizations(EwbRegistryAllOrganizationsRequest request);
    
    Page<EwbRegistryResponse> searchRegistryForSelfOrganization(EwbRegistrySelfOrganizationRequest request, UUID userId);
    
    /**
     * Ищем активные ЭПЛ по списку идентификаторов подразделений
     *
     * @param departmentIds список идентификаторов подразделений
     *
     * @return наличие ЭПЛ
     */
    boolean haveActiveEwb(List<UUID> departmentIds, LocalDate checkStartDate);
    
    /**
     * Получение детальной карточки ЭПЛ
     *
     * @param userId Идентификатор водителя
     *
     * @return Детальная информация по ЭПЛ {@link GetEwbDetailedDto}
     */
    GetEwbDetailedDto getEwbDetailed(UUID userId);
    
    /**
     * Метод для отмены ЭПЛ
     *
     * @param id идентификатор ЭПЛ
     * @param request данные об отмене ЭПЛ
     * @param userId пользователь инициировавший отмену ЭПЛ
     */
    void cancelEwb(UUID id, EwbCancelRequestDto request, UUID userId);
    
    /**
     * Метод для автоматического обновления статуса ЭПЛ шедулером.
     */
    void statusAutoUpdate();
    
    /**
     * Метод для ввода остатка литража
     *
     * @param request запрос с остатком литража {@link EwbLitreageOutRequest}
     * @param userId идентификатор пользователя
     *
     * @return ответ на запрос {@link CheckResponse}}
     */
    CheckResponse litreageOut(EwbLitreageOutRequest request, UUID userId);
    
    /**
     * Получение результатов осмотра и второго титула
     * @param titleUUID UUID титула
     * @return Результаты осмотра и второго титула
     */
    MedicalCheckUpModel getStraightSecondTitle(UUID titleUUID);
}
