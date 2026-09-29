package ru.sberbank.ditsib.transport.request.dto.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.mappers.*;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Проверка маппинга")
class MappingTest {
    
    private final EmployeeMapper employeeMapper = new EmployeeMapperImpl();
    private final VehicleMapper vehicleMapper = new VehicleMapperImpl();
    
    private final EntityDTOMapper mapper = new EntityDTOMapperImpl(employeeMapper, vehicleMapper, new FraudMapperImpl());
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final DepartmentService departmentService = mock(DepartmentService.class);
    
    @DisplayName("Проверка преобразования сотрудника для передачи")
    @Test
    void test_entityToDto() {
        when(departmentService.findOrCreateById(any())).thenReturn(Department.builder().id(UUID.randomUUID()).build());
        var userId = UUID.randomUUID();
        var id = UUID.randomUUID();
        var department = departmentService.findOrCreateById(UUID.randomUUID());
        var positionId = UUID.randomUUID();
        var supervisorId = UUID.randomUUID();
        var delegatedById = UUID.randomUUID();
        
        var dto = employeeMapper.toDto(Employee.builder().userId(userId).id(id).department(department)
                                               .firstName("FirstName")
                                               .humanReadableId("Human readable ID").lastName("Last name")
                                               .positionId(positionId)
                                               .patronymic("Patronymic").personnelNumber("Personnel number")
                                               .supervisorId(supervisorId).mobilePhone("Mobile").active(true).build());
        
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.firstName()).isEqualTo("FirstName");
        assertThat(dto.humanReadableId()).isEqualTo("Human readable ID");
        assertThat(dto.lastName()).isEqualTo("Last name");
        assertThat(dto.patronymic()).isEqualTo("Patronymic");
        assertThat(dto.personnelNumber()).isEqualTo("Personnel number");
        assertThat(dto.organizationId()).isNull();
        assertThat(dto.departmentId()).isEqualTo(department.getId());
        assertThat(dto.positionId()).isEqualTo(positionId);
        assertThat(dto.supervisorId()).isEqualTo(supervisorId);
        assertThat(dto.userId()).isEqualTo(userId);
    }
    
    @Test
    void testRequestDtoMapping() throws JsonProcessingException {
        String input = "{\n" +
                       "  \"passenger\": {\n" +
                       "    \"userId\": \"4a99fcea-f24a-4251-a445-36242f422d4d\",\n" +
                       "    \"firstName\": \"Владимир\",\n" +
                       "    \"lastName\": \"Захаров\",\n" +
                       "    \"patronymic\": \"Вадимович\",\n" +
                       "    \"personnelNumber\": \"123456789\",\n" +
                       "    \"departmentId\": \"51bd5768-84a7-4ea4-84b8-cbdb5b025459\",\n" +
                       "    \"positionId\": \"445baee2-e8b5-4ebb-80ad-00f3153ebebd\",\n" +
                       "    \"supervisorId\": \"28bca53a-4340-4d28-8f16-cefa09a5a5ae\",\n" +
                       "    \"id\": \"a44f202f-93d6-4d81-8de5-01c87d9bc6db\",\n" +
                       "    \"delegatedById\": \"\",\n" +
                       "    \"organizationId\": \"ab35770a-1d63-4b0e-a165-ea2c4f8c3adf\"\n" +
                       "  },\n" +
                       "  \"transportType\": \"TAXI\",\n" +
                       "  \"taxiClass\": \"COMFORT\",\n" +
                       "  \"passengerCount\": 1,\n" +
                       "  \"tariffId\": \"\",\n" +
                       "  \"desiredDate\": 1589931271097,\n" +
                       "  \"expected\": {\n" +
                       "    \"distance\": 7.4641,\n" +
                       "    \"time\": 570000,\n" +
                       "    \"segments\": [\n" +
                       "      {\n" +
                       "        \"distance\": 7.4641,\n" +
                       "        \"time\": 570000,\n" +
                       "        \"coordinates\": [\n" +
                       "          {\n" +
                       "            \"latitude\": 59.931797,\n" +
                       "            \"longitude\": 30.355474\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.932747,\n" +
                       "            \"longitude\": 30.347895\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.936058,\n" +
                       "            \"longitude\": 30.348089\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.93578,\n" +
                       "            \"longitude\": 30.364046\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.930965,\n" +
                       "            \"longitude\": 30.360826\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.930737,\n" +
                       "            \"longitude\": 30.361866\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.93066,\n" +
                       "            \"longitude\": 30.362328\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.927654,\n" +
                       "            \"longitude\": 30.37022\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.928535,\n" +
                       "            \"longitude\": 30.371447\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.923492,\n" +
                       "            \"longitude\": 30.385136\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.924351,\n" +
                       "            \"longitude\": 30.389486\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.926758,\n" +
                       "            \"longitude\": 30.401295\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.93187,\n" +
                       "            \"longitude\": 30.427853\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.931999,\n" +
                       "            \"longitude\": 30.428585\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.934402,\n" +
                       "            \"longitude\": 30.440367\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.934517,\n" +
                       "            \"longitude\": 30.440266\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.934376,\n" +
                       "            \"longitude\": 30.439594\n" +
                       "          },\n" +
                       "          {\n" +
                       "            \"latitude\": 59.934269,\n" +
                       "            \"longitude\": 30.437717\n" +
                       "          }\n" +
                       "        ]\n" +
                       "      }\n" +
                       "    ],\n" +
                       "    \"waypoints\": [\n" +
                       "      {\n" +
                       "        \"country\": \"RU\",\n" +
                       "        \"region\": \"Saint Petersburg\",\n" +
                       "        \"city\": \"Литейный округ\",\n" +
                       "        \"street\": \"100 Невский проспект\",\n" +
                       "        \"house\": null,\n" +
                       "        \"building\": null,\n" +
                       "        \"structure\": null,\n" +
                       "        \"latitude\": 59.93203,\n" +
                       "        \"longitude\": 30.35561,\n" +
                       "        \"waitTime\": null\n" +
                       "      },\n" +
                       "      {\n" +
                       "        \"country\": \"RU\",\n" +
                       "        \"region\": \"Saint Petersburg\",\n" +
                       "        \"city\": \"округ Малая Охта\",\n" +
                       "        \"street\": \"8 площадь Карла Фаберже\",\n" +
                       "        \"house\": null,\n" +
                       "        \"building\": null,\n" +
                       "        \"structure\": null,\n" +
                       "        \"latitude\": 59.93483,\n" +
                       "        \"longitude\": 30.4376,\n" +
                       "        \"waitTime\": null\n" +
                       "      }\n" +
                       "    ],\n" +
                       "    \"cost\": 380\n" +
                       "  },\n" +
                       "  \"purpose\": null\n" +
                       "}";
        
        String input1 = "{\n" +
                        "  \"id\": null,\n" +
                        "  \"author\": {\n" +
                        "    \"id\": \"f10bcc5b-51db-4e1c-a747-2a229604f974\",\n" +
                        "    \"userId\": null,\n" +
                        "    \"firstName\": null,\n" +
                        "    \"lastName\": null,\n" +
                        "    \"patronymic\": null,\n" +
                        "    \"personnelNumber\": null,\n" +
                        "    \"delegatedById\": null,\n" +
                        "    \"supervisorId\": null,\n" +
                        "    \"positionId\": null\n" +
                        "  },\n" +
                        "  \"passenger\": {\n" +
                        "    \"id\": \"558d39f2-c638-490f-90e9-94d896a65b4c\",\n" +
                        "    \"userId\": null,\n" +
                        "    \"firstName\": null,\n" +
                        "    \"lastName\": null,\n" +
                        "    \"patronymic\": null,\n" +
                        "    \"personnelNumber\": null,\n" +
                        "    \"delegatedById\": null,\n" +
                        "    \"supervisorId\": null,\n" +
                        "    \"positionId\": null\n" +
                        "  },\n" +
                        "  \"transportType\": \"TAXI\",\n" +
                        "  \"taxiClass\": \"ECONOMY\",\n" +
                        "  \"coopTrip\": false,\n" +
                        "  \"passengerCount\": 1,\n" +
                        "  \"tariffId\": null,\n" +
                        "  \"desiredDate\": 1589816483230,\n" +
                        "  \"expected\": {\n" +
                        "    \"cost\": 200.0,\n" +
                        "    \"distance\": 30.0,\n" +
                        "    \"time\": 1800000,\n" +
                        "    \"segments\": [\n" +
                        "      {\n" +
                        "        \"cost\": 0.0,\n" +
                        "        \"distance\": 0.0,\n" +
                        "        \"time\": 900000,\n" +
                        "        \"coordinates\": [\n" +
                        "          {\n" +
                        "            \"latitude\": 0.0,\n" +
                        "            \"longitude\": 0.0\n" +
                        "          },\n" +
                        "          {\n" +
                        "            \"latitude\": 0.0,\n" +
                        "            \"longitude\": 0.0\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"cost\": 0.0,\n" +
                        "        \"distance\": 0.0,\n" +
                        "        \"time\": 900000,\n" +
                        "        \"coordinates\": [\n" +
                        "          {\n" +
                        "            \"latitude\": 0.0,\n" +
                        "            \"longitude\": 0.0\n" +
                        "          },\n" +
                        "          {\n" +
                        "            \"latitude\": 0.0,\n" +
                        "            \"longitude\": 0.0\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"waypoints\": [\n" +
                        "      {\n" +
                        "        \"address\": {\n" +
                        "          \"country\": \"Country 1\",\n" +
                        "          \"region\": \"Region 1\",\n" +
                        "          \"city\": \"City 1\",\n" +
                        "          \"street\": \"Street 1\",\n" +
                        "          \"house\": \"House 1\",\n" +
                        "          \"building\": \"Building 1\",\n" +
                        "          \"structure\": \"Structure 1\",\n" +
                        "          \"latitude\": 0.0,\n" +
                        "          \"longitude\": 0.0\n" +
                        "        },\n" +
                        "        \"waitTime\": null\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"address\": {\n" +
                        "          \"country\": \"Country 2\",\n" +
                        "          \"region\": \"Region 2\",\n" +
                        "          \"city\": \"City 2\",\n" +
                        "          \"street\": \"Street 2\",\n" +
                        "          \"house\": \"House 2\",\n" +
                        "          \"building\": \"Building 2\",\n" +
                        "          \"structure\": \"Structure 2\",\n" +
                        "          \"latitude\": 1.0,\n" +
                        "          \"longitude\": 1.0\n" +
                        "        },\n" +
                        "        \"waitTime\": null\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"address\": {\n" +
                        "          \"country\": \"Country 3\",\n" +
                        "          \"region\": \"Region 3\",\n" +
                        "          \"city\": \"City 3\",\n" +
                        "          \"street\": \"Street 3\",\n" +
                        "          \"house\": \"House 3\",\n" +
                        "          \"building\": \"Building 3\",\n" +
                        "          \"structure\": \"Structure 3\",\n" +
                        "          \"latitude\": 2.0,\n" +
                        "          \"longitude\": 2.0\n" +
                        "        },\n" +
                        "        \"waitTime\": null\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  },\n" +
                        "  \"purpose\": {\n" +
                        "    \"purpose\": null,\n" +
                        "    \"id\": 1\n" +
                        "  },\n" +
                        "  \"comment\": \"Comment\",\n" +
                        "  \"status\": null,\n" +
                        "  \"creationTime\": null\n" +
                        "}";
        
        NewRequestDTO requestDTO = objectMapper.readValue(input, NewRequestDTO.class);
        System.out.println(requestDTO.toString());
        Request request = mapper.newDTOToRequestForTaxi(requestDTO);
        Employee employee = Employee.builder().id(UUID.fromString("4a99fcea-f24a-4251-a445-36242f422d4d")).build();
        Request req = RequestForTaxi.builder()
                                    .author(employee)
                                    .commentForDriver(requestDTO.getCommentForDriver())
                                    .desiredDate(requestDTO.getDesiredDate())
                                    .expected(mapper.dtoToExpectedData(requestDTO.getExpected()))
                                    .passenger(Employee.builder().id(requestDTO.getPassenger().id()).build())
                                    .passengerCount(requestDTO.getPassengerCount())
                                    .purpose(mapper.dtoToTripPurpose(requestDTO.getPurpose()))
                                    .tariffId(requestDTO.getTariffId())
                                    .taxiClass(requestDTO.getTaxiClass())
                                    .coopTrip(requestDTO.isCoopTrip())
                                    .build();
        
    }
    
    @Test
    void testEmployeeFromId() {
        UUID uuid = UUID.randomUUID();
        
        Employee employee = mapper.employeeFromId(uuid);
        
        Assertions.assertEquals(uuid, employee.getId());
    }
    
    @Test
    void testDepartmentFromNullId_returnsNull() {
        UUID uuid = UUID.randomUUID();
        
        Employee employee = mapper.employeeFromId(uuid);
        
        Assertions.assertEquals(uuid, employee.getId());
    }
    
    @Test
    void testNullMapsNull() {
        
        Assertions.assertNull(mapper.requestToDTO((Request) null));
        Assertions.assertNull(mapper.tripPurposeToDTO(null));
        Assertions.assertNull(employeeMapper.toDto((Employee) null));
        Assertions.assertNull(mapper.employeeFromId(null));
        Assertions.assertNull(mapper.employeeMessageToEmployeeEntity(null));
        Assertions.assertNull(mapper.expectedDataToDTO(null));
        Assertions.assertNull(mapper.newDTOToRequestForPersonal(null));
        Assertions.assertNull(mapper.newDTOToRequestForPublic(null));
        Assertions.assertNull(mapper.newDTOToRequestForTaxi(null));
        Assertions.assertNull(mapper.positionMessageToPosition(null));
        Assertions.assertNull(mapper.waypointDTOToAddress(null));
        Assertions.assertNull(mapper.coordinatesToDTO(null));
        Assertions.assertNull(mapper.dtoToExpectedData(null));
        Assertions.assertNull(mapper.dtoToTripPurpose(null));
        Assertions.assertNull(mapper.dtoToCoordinates(null));
        Assertions.assertNull(mapper.dtoToRequestForPersonal(null));
        Assertions.assertNull(mapper.dtoToRouteSegment(null));
        Assertions.assertEquals(Collections.emptyList(), mapper.routeSegmentDTOListToRouteSegmentList(null));
        
        
    }
    
}
