package ru.sber.transport.telemechanic.handler;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.LoggingExtension;
import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.database.model.Position;
import ru.sber.transport.telemechanic.dto.CreateRequestDto;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.exception.CheckNotFoundException;
import ru.sber.transport.telemechanic.service.CheckService;
import ru.sber.transport.telemechanic.service.EmployeeService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.TestData.EMPLOYEE_1_ID;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_DRIVER;

@DisplayName("Обрабокта rest исключений")
@SpringBootTest(properties = {"logging.level.ru.sberbank.ditsib=DEBUG",
        "spring.servlet.multipart.max-file-size=1MB"})
@EmbeddedPostgres
@AutoConfigureMockMvc
class RequestExceptionHandlerTest {
    
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private CheckService checkService;
    @MockitoBean
    private EmployeeService employeeService;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(RequestExceptionHandler.class);

    @SneakyThrows
    @Test
    void handleMethodArgumentNotValid() {
        var dtoString = """
                {
                   "transportId": null
                 }
                 """;
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_DRIVER.name());
        mockMvc.perform(post("/request/create")
                        .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                        .content(dtoString)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andExpect(result -> assertThat(Optional.ofNullable(result.getResolvedException()).orElseThrow().getMessage()).
                        contains("Validation failed for argument [0] in "));
        assertEquals(2, LOGGING_EXTENSION.getEvents().size());
        var firstEvent = LOGGING_EXTENSION.getEvents().get(0);
        var secondEvent = LOGGING_EXTENSION.getEvents().get(1);
        assertEquals(RequestExceptionHandler.class.getName(), firstEvent.getLoggerName());
        assertThat(firstEvent.getMessage()).contains("Validation failed for argument [0] in ");
        assertEquals(Level.DEBUG, firstEvent.getLevel());
        assertTrue(firstEvent.getThrowableProxy().getStackTraceElementProxyArray().length > 10);
        assertEquals(RequestExceptionHandler.class.getName(), secondEvent.getLoggerName());
        assertEquals("Не пройдены проверки по полям:" +
                "{transportId=Идентификатор транспорта не может быть пустым}", secondEvent.getMessage());
        assertEquals(Level.INFO, secondEvent.getLevel());
    }

    @SneakyThrows
    @Test
    void handleBusinessException() {
        var requestId = UUID.randomUUID();
        var checkType = CheckType.VEHICLE_NUMBER;
        var exceptionMessage = String.format("Проверка не найдена, id заявки:'%s', тип проверки:%s", requestId, checkType);
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_DRIVER.name());
        doThrow(new CheckNotFoundException(requestId, checkType)).when(checkService).get(requestId, checkType);
        mockMvc.perform(get("/request/" + requestId + "/" + checkType.name())
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(CheckNotFoundException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals(exceptionMessage,
                        Optional.ofNullable(result.getResolvedException()).orElseThrow().getMessage()));
        assertEquals(2, LOGGING_EXTENSION.getEvents().size());
        var firstEvent = LOGGING_EXTENSION.getEvents().get(0);
        var secondEvent = LOGGING_EXTENSION.getEvents().get(1);
        assertEquals(RequestExceptionHandler.class.getName(), firstEvent.getLoggerName());
        assertEquals(exceptionMessage, firstEvent.getFormattedMessage());
        assertEquals(Level.INFO, firstEvent.getLevel());
        assertEquals(RequestExceptionHandler.class.getName(), secondEvent.getLoggerName());
        assertEquals(exceptionMessage, secondEvent.getFormattedMessage());
        assertEquals(Level.DEBUG, secondEvent.getLevel());
        assertTrue(secondEvent.getThrowableProxy().getStackTraceElementProxyArray().length > 10);
    }

    @SneakyThrows
    @Test
    void handleDataIntegrityViolationException() {
        var requestId = UUID.randomUUID();
        var checkType = CheckType.VEHICLE_NUMBER;
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_DRIVER.name());
        doThrow(new DataIntegrityViolationException("text")).when(checkService).get(requestId, checkType);
        mockMvc.perform(get("/request/" + requestId + "/" + checkType.name())
                        .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isConflict())
                .andExpect(result -> assertInstanceOf(DataIntegrityViolationException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("text",
                        Optional.ofNullable(result.getResolvedException()).orElseThrow().getMessage()));
        assertEquals(2, LOGGING_EXTENSION.getEvents().size());
        var firstEvent = LOGGING_EXTENSION.getEvents().get(0);
        var secondEvent = LOGGING_EXTENSION.getEvents().get(1);
        assertEquals(RequestExceptionHandler.class.getName(), firstEvent.getLoggerName());
        assertEquals("Удаление невозможно в связи с наличием связных записей:text", firstEvent.getFormattedMessage());
        assertEquals(Level.INFO, firstEvent.getLevel());
        assertEquals(RequestExceptionHandler.class.getName(), secondEvent.getLoggerName());
        assertEquals("text", secondEvent.getFormattedMessage());
        assertEquals(Level.DEBUG, secondEvent.getLevel());
        assertTrue(secondEvent.getThrowableProxy().getStackTraceElementProxyArray().length > 10);
    }

    @SneakyThrows
    @Test
    void handleMultipartException() {
        var requestId = UUID.randomUUID();
        var checkType = CheckType.VEHICLE_NUMBER;
        var organization1 = Instancio.create(Organization.class);
        var department1 = Instancio.create(Department.class);
        var position1 = Instancio.of(Position.class)
                         .set(field(Position::getOrganization), organization1).create();
        var employee1 = Instancio.of(Employee.class)
                                 .set(field(Employee::getOrganization), organization1)
                                 .set(field(Employee::getDepartment), department1)
                                 .set(field(Employee::getPosition), position1)
                                 .create();
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_DRIVER.name());
        var file = new MockMultipartFile("files", "1111.jpg",
                                         MediaType.IMAGE_JPEG_VALUE,
                                         getClass().getClassLoader().getResourceAsStream("load/1111.jpg"));
        when(employeeService.getByUserId(any())).thenReturn(employee1);
        when(checkService.doCheck(requestId, checkType, new MultipartFile[]{ file }, null, employee1.getId()))
                .thenThrow(new MultipartException("Maximum upload size exceeded"));
        mockMvc.perform(multipart("/request/" + requestId + "/" + checkType + "/check")
                        .file(file)
                        .with(jwt().jwt(builder -> builder.jti(employee1.getId().toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name()))))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(result -> assertInstanceOf(MultipartException.class, result.getResolvedException()))
                .andExpect(result -> assertThat(Optional.ofNullable(result.getResolvedException()).orElseThrow().getMessage()).
                        contains("Maximum upload size exceeded"));
        assertEquals(2, LOGGING_EXTENSION.getEvents().size());
        var firstEvent = LOGGING_EXTENSION.getEvents().get(0);
        var secondEvent = LOGGING_EXTENSION.getEvents().get(1);
        assertEquals(RequestExceptionHandler.class.getName(), firstEvent.getLoggerName());
        assertEquals("Превышем максимальный размер для загружаемого файла:1MB", firstEvent.getFormattedMessage());
        assertEquals(Level.INFO, firstEvent.getLevel());
        assertEquals(RequestExceptionHandler.class.getName(), secondEvent.getLoggerName());
        assertEquals("Maximum upload size exceeded", secondEvent.getMessage());
        assertEquals(Level.DEBUG, secondEvent.getLevel());
        assertTrue(secondEvent.getThrowableProxy().getStackTraceElementProxyArray().length > 10);
    }
    
    @SneakyThrows
    @Test
    void handleTaskRejectedException() {
        var request = createDto();
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_DRIVER.name());
        doThrow(new TaskRejectedException("text")).when(employeeService).getByUserId(EMPLOYEE_1_ID);
        mockMvc.perform(post("/request/create")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isTooEarly())
               .andExpect(result -> assertInstanceOf(TaskRejectedException.class, result.getResolvedException()))
               .andExpect(result -> assertEquals("text",
                                                 Optional.ofNullable(result.getResolvedException()).orElseThrow().getMessage()));
        assertEquals(2, LOGGING_EXTENSION.getEvents().size());
        var firstEvent = LOGGING_EXTENSION.getEvents().get(0);
        var secondEvent = LOGGING_EXTENSION.getEvents().get(1);
        assertEquals(RequestExceptionHandler.class.getName(), firstEvent.getLoggerName());
        assertEquals("Попробуйте снова через несколько минут", firstEvent.getFormattedMessage());
        assertEquals(Level.INFO, firstEvent.getLevel());
        assertEquals(RequestExceptionHandler.class.getName(), secondEvent.getLoggerName());
        assertEquals("text", secondEvent.getFormattedMessage());
        assertEquals(Level.DEBUG, secondEvent.getLevel());
        assertTrue(secondEvent.getThrowableProxy().getStackTraceElementProxyArray().length > 10);
    }
    
    @SneakyThrows
    private String createDto() {
        return mapper.writeValueAsString(new CreateRequestDto(
                UUID.randomUUID()
        ));
    }
}