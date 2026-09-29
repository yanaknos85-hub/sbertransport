package ru.sberbank.ditsib.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.database.dao.LeadRepository;
import ru.sberbank.ditsib.database.dao.MainLeadRepository;
import ru.sberbank.ditsib.database.dao.PointLeadRepository;
import ru.sberbank.ditsib.database.model.Lead;
import ru.sberbank.ditsib.database.model.MainLead;
import ru.sberbank.ditsib.database.model.PointLead;
import ru.sberbank.ditsib.dto.AggregatedMainLeadDto;
import ru.sberbank.ditsib.dto.CreateMainLeadResponseDto;
import ru.sberbank.ditsib.dto.MainLeadRequestModel;
import ru.sberbank.ditsib.enumerate.MainLeadStatus;
import ru.sberbank.ditsib.enumerate.PointType;
import ru.sberbank.ditsib.mappers.MainLeadMapper;
import ru.sberbank.ditsib.service.MainLeadFeignClient;
import ru.sberbank.ditsib.service.MainLeadService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Адаптер для работы с внешним сервисом через Feign клиент
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MainLeadServiceImpl implements MainLeadService {

    private final MainLeadFeignClient mainLeadFeignClient;
    private final MainLeadMapper mainLeadMapper;
    private final MainLeadRepository mainLeadRepository;
    private final PointLeadRepository pointLeadRepository;
    private final LeadRepository leadRepository;

    /**
     * Метод для предсказания маршрута такси
     *
     * @param model доменная модель с данными для предсказания
     * @return доменная модель с результатом предсказания
     */
    public CreateMainLeadResponseDto predictRoute(MainLeadRequestModel model) {
        try {
            log.debug("Подготавливаем запрос на предсказание маршрута: {}", model);
            var request = mainLeadMapper.toDto(model);
            log.debug("Отправляем запрос на предсказание маршрута: {}", request);
            var response = mainLeadFeignClient.predict(request);
            log.debug("Получен ответ от сервиса предсказания: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Ошибка при вызове сервиса предсказания маршрута", e);
            throw new RuntimeException("Не удалось получить предсказание маршрута", e);
        }
    }


    @Override
    public List<AggregatedMainLeadDto> getAllRequests() {
        var mainLeads = mainLeadRepository.findAllByStatusIn(
                List.of(MainLeadStatus.GENERATING, MainLeadStatus.GENERATED));

        List<AggregatedMainLeadDto> content = new ArrayList<>();
        for (MainLead mainLead : mainLeads) {
            List<PointLead> points = pointLeadRepository.findAllByMainLeadId(mainLead.getId());
            var leads = leadRepository.findAllByMainLeadId(mainLead.getId());

            Integer cost = leads.stream().map(Lead::getCost).reduce(0, Integer::sum);
            var departureTime = leads.stream()
                    .map(Lead::getDepartureTime)
                    .min(Comparator.naturalOrder())
                    .orElse(null);

            String startPoint = points.stream().filter(p -> p.getTypePoint().equals(PointType.START))
                    .map(PointLead::getWaypoint).findFirst()
                    .orElse(null);

            String endPoint = points.stream().filter(p -> p.getTypePoint().equals(PointType.END))
                    .map(PointLead::getWaypoint).findFirst()
                    .orElse(null);
            int freeSeats = leads.get(0).getTransportType().getMaxPassengers() - leads.size();
            if (freeSeats < 0) {
                log.error("Ошибка при расчете свободных мест для основной заявки {}", mainLead.getId());
            }
            String geozone = null;
            //todo реализовать логику по получению геозоны, возможно добавить в поинты
            var dto = new AggregatedMainLeadDto(mainLead.getId(), leads.get(0).getTransportType(),
                    cost, freeSeats, departureTime, startPoint, endPoint, geozone, mainLead.getStatus());
            content.add(dto);
        }

        return content;
    }
} 