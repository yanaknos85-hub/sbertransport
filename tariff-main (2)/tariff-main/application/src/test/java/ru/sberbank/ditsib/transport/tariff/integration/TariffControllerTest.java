package ru.sberbank.ditsib.transport.tariff.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
@MockitoBean(types = JwtDecoder.class)
class TariffControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff.sql"})
    void search() {
        AuthorizeUtils.authorize(manager, "ROLE_ADMIN_CORP_CLIENT");
        mockMvc.perform(post("/search")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_CORP_CLIENT")))
                        .content("""
                                {
                                    "page": {
                                        "pageNumber": 0,
                                        "pageSize": 10
                                    },
                                    "active": "true",
                                    "serviceType": "EMPLOYEE_TRANSPORTATION"
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(6)))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(20))
                .andExpect(jsonPath("$.pageable.sort[0].property").value("humanReadableId"))
                .andExpect(jsonPath("$.pageable.sort[0].direction").value("ASC"))
                .andExpect(jsonPath("$.pageable.paged").value(true))
                .andExpect(jsonPath("$.pageable.unpaged").value(false))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.numberOfElements").value(6))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.sort[0].property").value("humanReadableId"))
                .andExpect(jsonPath("$.sort[0].direction").value("ASC"))
                .andExpect(jsonPath("$.sort[0].ascending").value(true))
                .andExpect(jsonPath("$.sort[0].descending").value(false))

                .andExpect(jsonPath("$.content[0].id").value("c44aafcb-fbef-4994-b471-5ce8164ecbad"))
                .andExpect(jsonPath("$.content[0].humanReadableId").value("TF-0001-00000469"))
                .andExpect(jsonPath("$.content[0].region").value("Богородский"))
                .andExpect(jsonPath("$.content[0].regionId").value("c0bcd639-eaa9-4680-95da-3c482aacbbca"))
                .andExpect(jsonPath("$.content[0].transportType").value("TAXI"))
                .andExpect(jsonPath("$.content[0].serviceType").value("EMPLOYEE_TRANSPORTATION"))
                .andExpect(jsonPath("$.content[0].organizationId").value("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"))
                .andExpect(jsonPath("$.content[0].contractId").value("99022e46-def7-4a1f-9a27-f47e6bf32a25"))
                .andExpect(jsonPath("$.content[0].contractNumber").value("1728391728"))
                .andExpect(jsonPath("$.content[0].contractorId").value("7e322ec2-f7a1-4683-a7b1-e720f7c07ed5"))
                .andExpect(jsonPath("$.content[0].contractorName").value("Контрагент проверка импорта"))

                .andExpect(jsonPath("$.content[1].id").value("7b4e03f0-2a49-429b-a0fe-4db78f1156e1"))
                .andExpect(jsonPath("$.content[1].humanReadableId").value("TF-0001-00000496"))
                .andExpect(jsonPath("$.content[1].region").value("Саратов, город"))
                .andExpect(jsonPath("$.content[1].regionId").value("28c65dcf-3142-48f7-bbe7-5b0dac13d704"))
                .andExpect(jsonPath("$.content[1].transportType").value("TAXI"))
                .andExpect(jsonPath("$.content[1].serviceType").value("EMPLOYEE_TRANSPORTATION"))
                .andExpect(jsonPath("$.content[1].organizationId").value("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"))
                .andExpect(jsonPath("$.content[1].contractId").value("8872fd8a-e7c2-44f3-a456-d1810fc28a64"))
                .andExpect(jsonPath("$.content[1].contractNumber").value("999123321"))
                .andExpect(jsonPath("$.content[1].contractorId").value("26f12ad5-ac3b-4355-9afc-da06bcfb7352"))
                .andExpect(jsonPath("$.content[1].contractorName").value("counterpartyUnisos"))

                .andExpect(jsonPath("$.content[2].id").value("356a6324-1dcc-4b75-b675-7dc0f0451161"))
                .andExpect(jsonPath("$.content[2].humanReadableId").value("TF-6336-00000010"))
                .andExpect(jsonPath("$.content[2].region").doesNotExist())
                .andExpect(jsonPath("$.content[2].regionId").value("c526065a-9d20-4889-9921-83adcb217b61"))
                .andExpect(jsonPath("$.content[2].transportType").value("PUBLIC"))
                .andExpect(jsonPath("$.content[2].serviceType").value("EMPLOYEE_TRANSPORTATION"))
                .andExpect(jsonPath("$.content[2].organizationId").value("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"))
                .andExpect(jsonPath("$.content[2].contractId").doesNotExist())
                .andExpect(jsonPath("$.content[2].contractNumber").doesNotExist())
                .andExpect(jsonPath("$.content[2].contractorId").doesNotExist())
                .andExpect(jsonPath("$.content[2].contractorName").doesNotExist())

                .andExpect(jsonPath("$.content[3].id").value("12da0888-995a-4077-bee7-cbef4f47a67b"))
                .andExpect(jsonPath("$.content[3].humanReadableId").value("TF-6336-00000011"))
                .andExpect(jsonPath("$.content[3].region").doesNotExist())
                .andExpect(jsonPath("$.content[3].regionId").value("c526065a-9d20-4889-9921-83adcb217b61"))
                .andExpect(jsonPath("$.content[3].transportType").value("PERSONAL"))
                .andExpect(jsonPath("$.content[3].serviceType").value("EMPLOYEE_TRANSPORTATION"))
                .andExpect(jsonPath("$.content[3].organizationId").value("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"))
                .andExpect(jsonPath("$.content[3].contractId").doesNotExist())
                .andExpect(jsonPath("$.content[3].contractNumber").doesNotExist())
                .andExpect(jsonPath("$.content[3].contractorId").doesNotExist())
                .andExpect(jsonPath("$.content[3].contractorName").doesNotExist())

                .andExpect(jsonPath("$.content[4].id").value("b6337524-a893-4de1-81d9-802a7dc2b152"))
                .andExpect(jsonPath("$.content[4].humanReadableId").value("TF-6336-00000012"))
                .andExpect(jsonPath("$.content[4].region").doesNotExist())
                .andExpect(jsonPath("$.content[4].regionId").value("c526065a-9d20-4889-9921-83adcb217b61"))
                .andExpect(jsonPath("$.content[4].transportType").value("CARSHARING"))
                .andExpect(jsonPath("$.content[4].serviceType").value("EMPLOYEE_TRANSPORTATION"))
                .andExpect(jsonPath("$.content[4].organizationId").value("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"))
                .andExpect(jsonPath("$.content[4].contractId").doesNotExist())
                .andExpect(jsonPath("$.content[4].contractNumber").doesNotExist())
                .andExpect(jsonPath("$.content[4].contractorId").doesNotExist())
                .andExpect(jsonPath("$.content[4].contractorName").doesNotExist())

                .andExpect(jsonPath("$.content[5].id").value("7ea09c52-7ed1-4423-8610-a782f63ef4b1"))
                .andExpect(jsonPath("$.content[5].humanReadableId").value("TF-6384-00000014"))
                .andExpect(jsonPath("$.content[5].region").value("Москва и Московская область"))
                .andExpect(jsonPath("$.content[5].regionId").value("c526065a-9d20-4889-9921-83adcb217b61"))
                .andExpect(jsonPath("$.content[5].transportType").value("GROUP_TRANSFER"))
                .andExpect(jsonPath("$.content[5].serviceType").value("EMPLOYEE_TRANSPORTATION"))
                .andExpect(jsonPath("$.content[5].organizationId").value("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"))
                .andExpect(jsonPath("$.content[5].contractId").value("81aad0fe-b30e-4ca4-8e83-af3d1a9bd233"))
                .andExpect(jsonPath("$.content[5].contractNumber").value("8988133791"))
                .andExpect(jsonPath("$.content[5].contractorId").value("d00cc6cd-d341-4641-9e5f-08b93dfea049"))
                .andExpect(jsonPath("$.content[5].contractorName").value("NQxLxLHCHn"))

                .andExpect(jsonPath("$.content[*].active", everyItem(is(true))))
                .andExpect(jsonPath("$.content[*].isNightTariff", everyItem(is(false))));
        var content = """
                {
                    "page": {
                        "pageNumber": 0,
                        "pageSize": 10
                    },
                    "active": "true",
                    "serviceType": "EMPLOYEE_TRANSPORTATION",
                    "transportClass":"COMFORT"
                }
                """;
        mockMvc.perform(post("/search")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_CORP_CLIENT")))
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(20))
                .andExpect(jsonPath("$.pageable.sort[0].property").value("humanReadableId"))
                .andExpect(jsonPath("$.pageable.sort[0].direction").value("ASC"))
                .andExpect(jsonPath("$.pageable.paged").value(true))
                .andExpect(jsonPath("$.pageable.unpaged").value(false))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.numberOfElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.sort[0].property").value("humanReadableId"))
                .andExpect(jsonPath("$.sort[0].direction").value("ASC"))
                .andExpect(jsonPath("$.sort[0].ascending").value(true))
                .andExpect(jsonPath("$.sort[0].descending").value(false))

                .andExpect(jsonPath("$.content[0].id").value("7ea09c52-7ed1-4423-8610-a782f63ef4b1"))
                .andExpect(jsonPath("$.content[0].humanReadableId").value("TF-6384-00000014"))
                .andExpect(jsonPath("$.content[0].region").value("Москва и Московская область"))
                .andExpect(jsonPath("$.content[0].regionId").value("c526065a-9d20-4889-9921-83adcb217b61"))
                .andExpect(jsonPath("$.content[0].transportType").value("GROUP_TRANSFER"))
                .andExpect(jsonPath("$.content[0].serviceType").value("EMPLOYEE_TRANSPORTATION"))
                .andExpect(jsonPath("$.content[0].organizationId").value("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"))
                .andExpect(jsonPath("$.content[0].contractId").value("81aad0fe-b30e-4ca4-8e83-af3d1a9bd233"))
                .andExpect(jsonPath("$.content[0].contractNumber").value("8988133791"))
                .andExpect(jsonPath("$.content[0].contractorId").value("d00cc6cd-d341-4641-9e5f-08b93dfea049"))
                .andExpect(jsonPath("$.content[0].contractorName").value("NQxLxLHCHn"))

                .andExpect(jsonPath("$.content[*].active", everyItem(is(true))))
                .andExpect(jsonPath("$.content[*].isNightTariff", everyItem(is(false))));
    }
}
