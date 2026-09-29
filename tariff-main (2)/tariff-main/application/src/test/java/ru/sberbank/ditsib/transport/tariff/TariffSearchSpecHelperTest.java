package ru.sberbank.ditsib.transport.tariff;

import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.dto.TariffSearchDTO;
import ru.sberbank.ditsib.transport.tariff.dto.TransportClass;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TariffSearchSpecHelperTest {
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery<BaseTariff> criteriaQuery;
    @Mock
    private Root<BaseTariff> root;
    @Mock
    private Predicate predicate;
    @Mock
    private Root<TaxiTariff> taxiTariffRoot;
    @Mock
    private Root<BaseTariffWithContract> baseTariffWithContractRoot;
    @Mock
    private Root<GroupTransferTariff> groupTransferTariffRoot;
    @Mock
    private Join<Object, Object> contractJoin;
    @Mock
    private Path<Object> organizationPath;
    @Mock
    private Path<Object> organizationIdPath;
    @Mock
    private Path<Object> humanReadableIdPath;
    @Mock
    private Path<Object> activePath;
    @Mock
    private Path<Object> regionIdPath;
    @Mock
    private Path<Object> serviceTypePath;
    @Mock
    private Path<Object> transportTypePath;
    @Mock
    private Path<Object> contractPath;
    @Mock
    private Path<Object> contractNumberPath;
    @Mock
    private Path<Object> contractorIdPath;
    @Mock
    private Path<Object> contractTypePath;
    @Mock
    private Path<Object> isNightTariffPath;
    @Mock
    private Path<Object> taxiClassPath;
    @Mock
    private Path<Object> groupTransferClassPath;
    @Mock
    private Expression<String> lowerExpression;

    @Test
    void getSpecificationWithoutFilters_whenDataMaster_shouldNotAddOrganizationPredicate() {
        var organizationId = UUID.randomUUID();
        var isDataMaster = true;
        doReturn(predicate).when(criteriaBuilder).and();
        var specification = TariffSearchSpecHelper
                .getSpecificationWithoutFilters(organizationId, isDataMaster);
        var result = specification.toPredicate(root, criteriaQuery, criteriaBuilder);
        assertThat(result).isEqualTo(predicate);
        verify(criteriaQuery, never()).distinct(anyBoolean());
        verify(criteriaBuilder, never()).equal(any(), any());
    }

    @Test
    void getSpecification_withAllFilters_shouldApplyAllPredicates() {
        var organizationId = UUID.randomUUID();
        var contractorId = UUID.randomUUID();
        var regionId = UUID.randomUUID();
        var contractId = UUID.randomUUID();

        var tariffSearchDTO = new TariffSearchDTO(
                TransportTypeEnum.TAXI,
                TransportServiceType.EMPLOYEE_TRANSPORTATION,
                organizationId,
                contractId,
                "CN-12345",
                contractorId,
                "TARIFF-001",
                regionId,
                TransportClass.COMFORT,
                true,
                true,
                ContractType.INCOME
        );

        doReturn(predicate).when(criteriaBuilder).and();
        doReturn(predicate).when(criteriaBuilder).equal(serviceTypePath, TransportServiceType.EMPLOYEE_TRANSPORTATION);
        doReturn(predicate).when(criteriaBuilder).like(any(), anyString());
        doReturn(lowerExpression).when(criteriaBuilder).lower(any());
        doReturn(taxiTariffRoot).when(criteriaBuilder).treat(root, TaxiTariff.class);
        doReturn(groupTransferTariffRoot).when(criteriaBuilder).treat(root, GroupTransferTariff.class);
        doReturn(baseTariffWithContractRoot).when(criteriaBuilder).treat(root, BaseTariffWithContract.class);
        doReturn(contractJoin).when(baseTariffWithContractRoot).join(BaseTariffWithContract_.CONTRACT);

        doReturn(serviceTypePath).when(root).get(BaseTariff_.SERVICE_TYPE);
        doReturn(transportTypePath).when(root).get(BaseTariff_.TRANSPORT_TYPE);
        doReturn(organizationPath).when(root).get(BaseTariff_.ORGANIZATION);
        doReturn(organizationIdPath).when(organizationPath).get(Organization_.ID);
        doReturn(humanReadableIdPath).when(root).get(BaseTariff_.HUMAN_READABLE_ID);
        doReturn(activePath).when(root).get(BaseTariff_.ACTIVE);
        doReturn(regionIdPath).when(root).get(BaseTariff_.REGION_ID);
        doReturn(contractPath).when(baseTariffWithContractRoot).get(BaseTariffWithContract_.CONTRACT);
        doReturn(contractNumberPath).when(contractPath).get(Contract_.CONTRACT_NUMBER);
        doReturn(isNightTariffPath).when(taxiTariffRoot).get(TaxiTariff_.IS_NIGHT_TARIFF);
        doReturn(taxiClassPath).when(taxiTariffRoot).get(TaxiTariff_.TAXI_CLASS);
        doReturn(contractorIdPath).when(contractJoin).get(Contract_.CONTRACTOR_ID);
        doReturn(contractTypePath).when(contractJoin).get(Contract_.CONTRACT_TYPE);
        doReturn(groupTransferClassPath).when(groupTransferTariffRoot).get(GroupTransferTariff_.GROUP_TRANSFER_CLASS);

        var specification = TariffSearchSpecHelper.getSpecification(tariffSearchDTO, organizationId, false);
        specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        verify(criteriaQuery, never()).distinct(anyBoolean());
        verify(criteriaBuilder).and(predicate, predicate);
        verify(criteriaBuilder).equal(serviceTypePath, TransportServiceType.EMPLOYEE_TRANSPORTATION);
        verify(criteriaBuilder).equal(transportTypePath, TransportTypeEnum.TAXI);
        verify(criteriaBuilder).equal(contractNumberPath, "CN-12345");
        verify(criteriaBuilder).equal(contractPath, new Contract(contractId));
        verify(criteriaBuilder).equal(contractTypePath, ContractType.INCOME);
        verify(criteriaBuilder).equal(contractorIdPath, contractorId);
        verify(criteriaBuilder, times(2)).equal(organizationIdPath, organizationId);
        verify(criteriaBuilder).like(lowerExpression, "%tariff-001%");
        verify(criteriaBuilder).equal(activePath, true);
        verify(criteriaBuilder).equal(regionIdPath, regionId);
        verify(criteriaBuilder).equal(isNightTariffPath, true);
        verify(criteriaBuilder).equal(taxiClassPath, TaxiClass.COMFORT);
    }

    @Test
    void getSpecification_withNullDTO_shouldApplyOnlyOrganizationPredicate() {
        var organizationId = UUID.randomUUID();
        doReturn(predicate).when(criteriaBuilder).and();
        var specification = TariffSearchSpecHelper.getSpecification(null, organizationId, false);
        specification.toPredicate(root, criteriaQuery, criteriaBuilder);
        verify(criteriaQuery, never()).distinct(anyBoolean());
    }
}