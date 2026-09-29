package ru.sber.transport.journal.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.journal.dto.*;
import ru.sber.transport.journal.service.JournalService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JournalControllerImpl.class)
@ContextConfiguration(classes = JournalControllerImpl.class)
class JournalControllerImplTest {
    
    private static final String CONTROLLER_URL = "/journal";
    private static final UUID EMPLOYEE_1_ID = UUID.randomUUID();
    private static final String ROLE_EMPLOYEE_CORP_CLIENT_NAME = "ROLE_EMPLOYEE_CORP_CLIENT";
    private final EasyRandom RANDOM = new EasyRandom(new EasyRandomParameters().seed(3177));
    
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private JournalService journalService;
    @MockitoBean
    private AuthorizationManager<?> manager;
    
    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(manager, ROLE_EMPLOYEE_CORP_CLIENT_NAME);
    }
    
    @SneakyThrows
    @Test
    void getTest() {
        var expected = RANDOM.nextObject(AbstractGetRequestDto.class);
        when(journalService.get(expected.getId(), EMPLOYEE_1_ID)).thenReturn(expected);
        var response = mockMvc.perform(get(CONTROLLER_URL + "/" + expected.getId())
                                               .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                          .authorities(new SimpleGrantedAuthority(ROLE_EMPLOYEE_CORP_CLIENT_NAME))))
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse();
        var actual = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), AbstractGetRequestDto.class);
        verify(journalService).get(any(), any());
        assertThat(actual)
                .usingRecursiveComparison(RecursiveComparisonConfiguration
                                                  .builder()
                                                  .withComparatorForType(
                                                          Comparator.comparing((LocalDateTime o) -> o.truncatedTo(ChronoUnit.SECONDS)),
                                                          LocalDateTime.class)
                                                  .build())
                .isEqualTo(expected);
    }
    
    @SneakyThrows
    @Test
    void getStatusHistory() {
        var requestId = UUID.randomUUID();
        var expected1 = RANDOM.nextObject(GetStatusDto.class);
        var expected2 = RANDOM.nextObject(GetStatusDto.class);
        when(journalService.getStatusHistory(requestId, EMPLOYEE_1_ID)).thenReturn(List.of(expected1, expected2));
        var response = mockMvc.perform(get(CONTROLLER_URL + "/" + requestId + "/status/history")
                                               .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                          .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE_CORP_CLIENT"))))
                              .andExpect(status().isOk())
                              .andReturn();
        verify(journalService).getStatusHistory(any(), any());
        var actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                            new TypeReference<List<GetStatusDto>>() {
                                            });
        assertThat(actual)
                .usingRecursiveComparison(RecursiveComparisonConfiguration
                                                  .builder()
                                                  .withComparatorForType(
                                                          Comparator.comparing((LocalDateTime o) -> o.truncatedTo(ChronoUnit.SECONDS)),
                                                          LocalDateTime.class)
                                                  .build())
                .isEqualTo(List.of(expected1, expected2));
    }
    
    @SneakyThrows
    @Test
    void cancel() {
        var requestId = UUID.randomUUID();
        doNothing().when(journalService).cancel(requestId, EMPLOYEE_1_ID);
        mockMvc.perform(patch(CONTROLLER_URL + "/" + requestId + "/cancel")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_EMPLOYEE_CORP_CLIENT_NAME))))
               .andExpect(status().isOk());
        verify(journalService).cancel(any(), any());
    }
    
    @SneakyThrows
    @Test
    void complete() {
        var requestId = UUID.randomUUID();
        doNothing().when(journalService).complete(requestId, EMPLOYEE_1_ID);
        mockMvc.perform(patch(CONTROLLER_URL + "/" + requestId + "/complete")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_EMPLOYEE_CORP_CLIENT_NAME))))
               .andExpect(status().isOk());
        verify(journalService).complete(any(), any());
    }
    
    @SneakyThrows
    @Test
    void addRevision() {
        var requestId = UUID.randomUUID();
        var comment = RANDOM.nextObject(String.class);
        doNothing().when(journalService).addRevision(requestId, comment, EMPLOYEE_1_ID);
        mockMvc.perform(patch(CONTROLLER_URL + "/" + requestId + "/revision")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_EMPLOYEE_CORP_CLIENT_NAME)))
                                .param("comment", comment))
               .andExpect(status().isOk());
        verify(journalService).addRevision(any(), any(), any());
    }
    
    @SneakyThrows
    @Test
    void addEvaluation() {
        var requestId = UUID.randomUUID();
        doNothing().when(journalService).addEvaluation(any(UUID.class), any(EvaluationDto.class), any(UUID.class));
        mockMvc.perform(patch(CONTROLLER_URL + "/" + requestId + "/evaluation")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_EMPLOYEE_CORP_CLIENT_NAME)))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content("""
                                         {
                                           "rating": 5,
                                           "comment": "Nice job",
                                           "reasons": ["reason1", "reason2"]
                                         }
                                         """))
               .andExpect(status().isOk());
        verify(journalService).addEvaluation(any(), any(), any());
    }
    
    @SneakyThrows
    @Test
    void getCompletedBySelf() {
        var dto1 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto2 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto3 = RANDOM.nextObject(GetRequestJournalDto.class);
        var pageRequest = PageRequest.of(0, 7);
        when(journalService.getCompletedBySelf(any(JournalDto.class), any(UUID.class)))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2, dto3), pageRequest, 3));
        var response = mockMvc.perform(
                                      post(CONTROLLER_URL + "/self/completed")
                                              .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_EMPLOYEE_CORP_CLIENT_NAME)))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(buildSearchDtoString()))
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse();
        verify(journalService).getCompletedBySelf(any(), any());
        var actual = parseJournalResponse(response);
        assertThat(actual)
                .usingRecursiveComparison(RecursiveComparisonConfiguration
                                                  .builder()
                                                  .withComparatorForType(
                                                          Comparator.comparing((LocalDateTime o) -> o.truncatedTo(ChronoUnit.SECONDS)),
                                                          LocalDateTime.class)
                                                  .build())
                .isEqualTo(List.of(dto1, dto2, dto3));
    }
    
    @SneakyThrows
    @Test
    void getActiveBySelf() {
        var dto1 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto2 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto3 = RANDOM.nextObject(GetRequestJournalDto.class);
        var pageRequest = PageRequest.of(0, 7);
        when(journalService.getActiveBySelf(any(JournalDto.class), any(UUID.class)))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2, dto3), pageRequest, 3));
        var response = mockMvc.perform(
                                      post(CONTROLLER_URL + "/self/active")
                                              .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_EMPLOYEE_CORP_CLIENT_NAME)))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(buildSearchDtoString()))
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse();
        verify(journalService).getActiveBySelf(any(), any());
        var actual = parseJournalResponse(response);
        assertThat(actual)
                .usingRecursiveComparison(RecursiveComparisonConfiguration
                                                  .builder()
                                                  .withComparatorForType(
                                                          Comparator.comparing((LocalDateTime o) -> o.truncatedTo(ChronoUnit.SECONDS)),
                                                          LocalDateTime.class)
                                                  .build())
                .isEqualTo(List.of(dto1, dto2, dto3));
    }
    
    @SneakyThrows
    @Test
    void getCompletedByStructure() {
        var dto1 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto2 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto3 = RANDOM.nextObject(GetRequestJournalDto.class);
        var pageRequest = PageRequest.of(0, 7);
        when(journalService.getCompletedByStructure(any(JournalDto.class), any(UUID.class)))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2, dto3), pageRequest, 3));
        var response = mockMvc.perform(
                                      post(CONTROLLER_URL + "/structure/completed")
                                              .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_EMPLOYEE_CORP_CLIENT_NAME)))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(buildSearchDtoString()))
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse();
        verify(journalService).getCompletedByStructure(any(), any());
        var actual = parseJournalResponse(response);
        assertThat(actual)
                .usingRecursiveComparison(RecursiveComparisonConfiguration
                                                  .builder()
                                                  .withComparatorForType(
                                                          Comparator.comparing((LocalDateTime o) -> o.truncatedTo(ChronoUnit.SECONDS)),
                                                          LocalDateTime.class)
                                                  .build())
                .isEqualTo(List.of(dto1, dto2, dto3));
    }
    
    @SneakyThrows
    @Test
    void getActiveByStructure() {
        var dto1 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto2 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto3 = RANDOM.nextObject(GetRequestJournalDto.class);
        var pageRequest = PageRequest.of(0, 7);
        when(journalService.getActiveByStructure(any(JournalDto.class), any(UUID.class)))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2, dto3), pageRequest, 3));
        var response = mockMvc.perform(
                                      post(CONTROLLER_URL + "/structure/active")
                                              .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                         .authorities(new SimpleGrantedAuthority(ROLE_EMPLOYEE_CORP_CLIENT_NAME)))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(buildSearchDtoString()))
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse();
        verify(journalService).getActiveByStructure(any(), any());
        var actual = parseJournalResponse(response);
        assertThat(actual)
                .usingRecursiveComparison(RecursiveComparisonConfiguration
                                                  .builder()
                                                  .withComparatorForType(
                                                          Comparator.comparing((LocalDateTime o) -> o.truncatedTo(ChronoUnit.SECONDS)),
                                                          LocalDateTime.class)
                                                  .build())
                .isEqualTo(List.of(dto1, dto2, dto3));
    }
    
    private String buildSearchDtoString() {
        return """
               {
                   "sortSetting": {
                       "property": "CREATION_DATE",
                       "directionAsc": false
                   },
                   "pageSetting": {
                       "page": 0,
                       "size": 7
                   }
               }""";
    }
    
    private List<GetRequestJournalDto> parseJournalResponse(MockHttpServletResponse response) throws IOException {
        var content = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8),
                                             new TypeReference<Map<String, Object>>() {
                                             });
        byte[] bytes = new ObjectMapper().writeValueAsBytes(content.get("content"));
        var actualList = objectMapper.readValue(bytes, new TypeReference<List<GetRequestJournalDto>>() {
        });
        assertNotNull(actualList);
        return actualList;
    }
}