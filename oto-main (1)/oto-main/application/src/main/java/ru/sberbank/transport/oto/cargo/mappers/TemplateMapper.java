package ru.sberbank.transport.oto.cargo.mappers;

import org.mapstruct.*;
import org.springframework.scheduling.support.CronExpression;
import ru.sberbank.ditsib.transport.request.messaging.TemplateForCargoMessage;
import ru.sberbank.transport.oto.cargo.database.model.template.TemplateForCargo;
import ru.sberbank.transport.oto.cargo.dto.oto.GetTemplateForCargoForOtoDto;
import ru.sberbank.transport.oto.cargo.exception.CronNotCorrectException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static ru.sberbank.transport.oto.cargo.dto.oto.GetTemplateForCargoForOtoDto.CargoPeriodDto.CargoPeriodEnum.MONTH;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        builder = @Builder(disableBuilder = true),
        imports = {TemplateForCargoMessage.class})
public interface TemplateMapper {
    @Mapping(target = "author.id", source = "template.authorId")
    @Mapping(target = "senderName",
             expression = "java(getWaypoint(source.getTemplate().getWaypoints(), " +
                          "java.util.function.BinaryOperator.minBy(" +
                          "java.util.Comparator.comparingInt(TemplateForCargoMessage.Waypoint::getOrderingIndex)" +
                          ")).getContact().getFio())")
    @Mapping(target = "recipientName",
             expression = "java(getWaypoint(source.getTemplate().getWaypoints(), " +
                          "java.util.function.BinaryOperator.maxBy(" +
                          "java.util.Comparator.comparingInt(TemplateForCargoMessage.Waypoint::getOrderingIndex)" +
                          ")).getContact().getFio())")
    @Mapping(target = "senderAddress",
             expression = "java(getWaypoint(source.getTemplate().getWaypoints(), " +
                          "java.util.function.BinaryOperator.minBy(" +
                          "java.util.Comparator.comparingInt(TemplateForCargoMessage.Waypoint::getOrderingIndex)" +
                          ")).getAddressStringRepresentation())")
    @Mapping(target = "recipientAddress",
             expression = "java(getWaypoint(source.getTemplate().getWaypoints(), " +
                          "java.util.function.BinaryOperator.maxBy(" +
                          "java.util.Comparator.comparingInt(TemplateForCargoMessage.Waypoint::getOrderingIndex)" +
                          ")).getAddressStringRepresentation())")
    TemplateForCargo toEntity(TemplateForCargoMessage source);
    
    @Mapping(target = "tariffType", source = "transportType")
    @Mapping(target = "nextRequestDate", source = "requestsDateDelivery", qualifiedByName = "getNextRequestDate")
    @Mapping(target = "lastRequestDate", source = "requestsDateDelivery", qualifiedByName = "getLastRequestDate")
    @Mapping(target = "author", source = "template.authorFIO")
    @Mapping(target = "authorMobilePhone", source = "template.authorPhone")
    @Mapping(target = "carrier", source = "template.contragent")
    @Mapping(target = "senderOrganization",
             expression = "java(getWaypoint(source.getTemplate().getWaypoints(), " +
                          "java.util.function.BinaryOperator.minBy(" +
                          "java.util.Comparator.comparingInt(TemplateForCargoMessage.Waypoint::getOrderingIndex)" +
                          ")).getOrganization())")
    @Mapping(target = "sender", source = "senderName")
    @Mapping(target = "senderPhone",
             expression = "java(getWaypoint(source.getTemplate().getWaypoints(), " +
                          "java.util.function.BinaryOperator.minBy(" +
                          "java.util.Comparator.comparingInt(TemplateForCargoMessage.Waypoint::getOrderingIndex)" +
                          ")).getContact().getPhone())")
    @Mapping(target = "recipientOrganization",
             expression = "java(getWaypoint(source.getTemplate().getWaypoints(), " +
                          "java.util.function.BinaryOperator.maxBy(" +
                          "java.util.Comparator.comparingInt(TemplateForCargoMessage.Waypoint::getOrderingIndex)" +
                          ")).getOrganization())")
    @Mapping(target = "recipient", source = "recipientName")
    @Mapping(target = "recipientPhone",
             expression = "java(getWaypoint(source.getTemplate().getWaypoints(), " +
                          "java.util.function.BinaryOperator.maxBy(" +
                          "java.util.Comparator.comparingInt(TemplateForCargoMessage.Waypoint::getOrderingIndex)" +
                          ")).getContact().getPhone())")
    @Mapping(target = "plannedRange", source = "template.distance")
    @Mapping(target = "plannedPrice", source = "totalCost")
    @Mapping(target = "cargoType", source = "template.cargoName")
    @Mapping(target = "loaders", source = "template.loaders")
    @Mapping(target = "volume", source = "template.volume")
    @Mapping(target = "weight", source = "template.weight")
    @Mapping(target = "comment", source = "template.comment")
    @Mapping(target = "period", source = "cronExpression", qualifiedByName = "periodDtoToString")
    @Mapping(target = "status", source = "status.description")
    GetTemplateForCargoForOtoDto entityToTemplateForOtoDto(TemplateForCargo source);
    
    default <T> T getWaypoint(List<T> waypoints, BinaryOperator<T> operator) {
        if (waypoints == null || waypoints.isEmpty()) {
            return null;
        }
        return waypoints.stream().reduce(operator).orElseThrow();
    }
    
    private GetTemplateForCargoForOtoDto.CargoPeriodDto getPeriod(String cronExpression) {
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("No valid cronExpression " + cronExpression);
        }
        var period = GetTemplateForCargoForOtoDto.CargoPeriodDto.builder().build();
        String[] cronFields = cronExpression.split(" +");
        var months = cronFields[4];
        if (!months.equals("*")) {
            period.setPeriodType(GetTemplateForCargoForOtoDto.CargoPeriodDto.CargoPeriodEnum.QUARTER);
            var monthList = months.split(",");
            period.setMonthOfQuartal(Arrays.stream(monthList)
                                           .mapToInt(Integer::valueOf).filter(t -> t < 4).boxed().collect(Collectors.toSet()));
        }
        var weeks = cronFields[5].split(",");
        Set<Integer> daySet = new HashSet<>();
        Set<Integer> weekSet = new HashSet<>();
        for (String value : weeks) {
            var idx = value.lastIndexOf('#');
            if (idx != -1) {
                if (idx == 0) {
                    throw new IllegalArgumentException("No day-of-week before '#' in '" + value + "'");
                } else if (idx == value.length() - 1) {
                    throw new IllegalArgumentException("No ordinal after '#' in '" + value + "'");
                }
                daySet.add(Integer.parseInt(value.substring(0, idx)));
                weekSet.add(Integer.parseInt(value.substring(idx + 1)));
            } else {
                daySet.add(Integer.parseInt(value));
            }
        }
        period.setDayOfWeek(daySet);
        period.setWeekOfMonth(weekSet);
        if (Objects.isNull(period.getPeriodType())) {
            if (weekSet.isEmpty()) {
                period.setPeriodType(GetTemplateForCargoForOtoDto.CargoPeriodDto.CargoPeriodEnum.WEEK);
            } else {
                period.setPeriodType(MONTH);
            }
        }
        if (!checkCronExpression(period)) {
            throw new CronNotCorrectException(period.getPeriodType().name());
        }
        return period;
    }
    
    @Named("periodDtoToString")
    default String cargoPeriodDtoToString(String cronExpression) {
        
        GetTemplateForCargoForOtoDto.CargoPeriodDto period = getPeriod(cronExpression);
        
        StringBuilder result = new StringBuilder();
        
        switch (period.getPeriodType()) {
            case QUARTER:
                result.append("Ежеквартально:").append("\n");
                if (period.getMonthOfQuartal() != null) {
                    result.append("Месяц: ").append(formatSet(period.getMonthOfQuartal())).append("\n");
                }
                if (period.getWeekOfMonth() != null) {
                    result.append("Неделя: ").append(formatSet(period.getWeekOfMonth())).append("\n");
                }
                if (period.getDayOfWeek() != null) {
                    result.append("Дни: ").append(formatDays(period.getDayOfWeek())).append("\n");
                }
                break;
            
            case MONTH:
                result.append("Ежемесячно:").append("\n");
                if (period.getWeekOfMonth() != null) {
                    result.append("Неделя: ").append(formatSet(period.getWeekOfMonth())).append("\n");
                }
                if (period.getDayOfWeek() != null) {
                    result.append("Дни: ").append(formatDays(period.getDayOfWeek())).append("\n");
                }
                break;
            
            case WEEK:
                result.append("Еженедельно:").append("\n");
                
                if (period.getDayOfWeek() != null) {
                    result.append("Дни: ").append(formatDays(period.getDayOfWeek())).append("\n");
                }
                break;
            
            default:
                throw new IllegalArgumentException("Unknown period type: " + period.getPeriodType());
            
        }
        
        return result.toString();
    }
    
    private String formatSet(Set<Integer> set) {
        return set.stream()
                  .sorted()
                  .map(String::valueOf)
                  .collect(Collectors.joining(" / "));
    }
    private String formatDays(Set<Integer> days) {
        Map<Integer, String> dayNames = Map.of(1, "Пн", 2, "Вт", 3, "Ср", 4, "Чт", 5, "Пт", 6, "Сб", 7, "Вс");
        return days.stream().sorted().map(dayNames::get)
                .collect(Collectors.joining(" / "));
    }
    
    
    /**
     * флк периода
     *
     * @param period период
     *
     * @return результат проверки - истина ложь
     */
    static boolean checkCronExpression(GetTemplateForCargoForOtoDto.CargoPeriodDto period) {
        return switch (period.getPeriodType()) {
            case MONTH -> Objects.nonNull(period.getDayOfWeek()) &&
                          !period.getDayOfWeek().isEmpty() &&
                          Objects.nonNull(period.getWeekOfMonth()) &&
                          period.getDayOfWeek().stream().filter(t -> t > 7).findFirst().isEmpty() &&
                          !period.getWeekOfMonth().isEmpty() &&
                          period.getWeekOfMonth().stream().filter(t -> t > 4).findFirst().isEmpty();
            case QUARTER -> Objects.nonNull(period.getDayOfWeek())
                            && Objects.nonNull(period.getWeekOfMonth())
                            && Objects.nonNull(period.getMonthOfQuartal()) &&
                            period.getDayOfWeek().stream().filter(t -> t > 7).findFirst().isEmpty() &&
                            !period.getDayOfWeek().isEmpty() && !period.getWeekOfMonth().isEmpty() &&
                            period.getWeekOfMonth().stream().filter(t -> t > 4).findFirst().isEmpty();
            case WEEK -> Objects.nonNull(period.getDayOfWeek()) && !period.getDayOfWeek().isEmpty() &&
                         period.getDayOfWeek().stream().filter(t -> t > 7).findFirst().isEmpty();
        };
    }
    
    @Named("getNextRequestDate")
    default LocalDateTime nextDate(List<LocalDateTime> requestDates) {
        return requestDates.stream()
                           .min(Comparator.comparing(date -> Duration.between(LocalDateTime.now(), date).abs()))
                           .orElse(null);
    }
    
    @Named("getLastRequestDate")
    default LocalDateTime lastDate(List<LocalDateTime> requestDates) {
        return requestDates.stream()
                           .max(Comparator.naturalOrder())
                           .orElse(null);
    }
}
