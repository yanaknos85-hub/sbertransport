package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sberbank.ditsib.transport.request.database.model.Request_;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "Поиск заявки", description = "Фильтры для поиска заявок")
public class RequestDriverSearchDTO extends DriverSearchDTO {
    
    @Schema(description = "Статусы водителя")
    private String driverStatuses;
    
    @Schema(description = "Автопарки")
    private String autoparkNames;
    
    @Schema(description = "Имя")
    private String firstName;
    
    @Schema(description = "Фамилия")
    private String lastName;
    
    @Schema(description = "Отчество")
    private String patronymic;
    
    @Schema(description = "Признаки водителей")
    private String driverTags;
    
    @Schema(description = "Опыт вождения")
    private String driverExp;
    
    @Schema(description = "Рейтинг водителя")
    private Integer driverRating;
    
    public static PageRequest getDriverPageRequest(RequestDriverSearchDTO driverSearchDTO) {
    return getDriverPageRequest(driverSearchDTO,driverSort(driverSearchDTO));
    }
        
        public static PageRequest getDriverPageRequest(RequestDriverSearchDTO driverSearchDTO, @NotNull Sort sort) {
        if (driverSearchDTO.getPageSetting() == null) {
            return PageRequest.of(0, 20, sort);
        }
        return PageRequest.of(driverSearchDTO.getPageSetting().getPage(), driverSearchDTO.getPageSetting().getSize(), sort);
    }
    
    @NotNull
    public static Sort driverSort(DriverSearchDTO driverSearchDTO) {
        if (driverSearchDTO.getDriverSortSetting() == null) {
            return Sort.by(Sort.Direction.ASC, Request_.DESIRED_DATE);
        }
        
        Sort sort;
        Sort.TypedSort<Driver> request = Sort.sort(Driver.class);
        switch (driverSearchDTO.getDriverSortSetting().getProperty()) {
            case FIRST_NAME: {
                Sort sortFirstName =
                        Sort.sort(Driver.class).by(Driver::getFirstName);
                sort = request.and(sortFirstName);
                break;
            }
            case LAST_NAME: {
                Sort sortLastName =
                        Sort.sort(Driver.class).by(Driver::getLastName);
                sort = request.and(sortLastName);
                break;
            }
            case PATRONYMIC: {
                Sort sortPatronymic =
                        Sort.sort(Driver.class).by(Driver::getPatronymic);
                sort = request.and(sortPatronymic);
                break;
            }
            case DRIVER_STATUS: {
                Sort sortStatus =
                        Sort.sort(Driver.class).by(Driver::getActive);
                sort = request.and(sortStatus);
                break;
            }
            case AUTOPARK_NAME: {
                Sort sortAutopark =
                        Sort.sort(Driver.class).by(Driver::getAutoparkId);
                sort = request.and(sortAutopark);
                break;
            }
            case DRIVER_TAG: {
                Sort sortByTag =
                        Sort.sort(Driver.class).by(Driver::getTags);
                sort = request.and(sortByTag);
                break;
            }
            case DRIVER_EXPIRIENCE: {
                Sort sortByExp=
                        Sort.sort(Driver.class).by(Driver::getExperience);
                sort = request.and(sortByExp);
                break;
            }
            case DRIVER_RAITING: {
                Sort sortRating =
                        Sort.sort(Driver.class).by(Driver::getRating);
                sort = request.and(sortRating);
                break;
            }
            
            default: {
                sort = Sort.by(Request_.CREATION_TIME);
            }
        }
        
        return driverSearchDTO.getDriverSortSetting().isDirectionAsc() ? sort.ascending() : sort.descending();
    }
}
