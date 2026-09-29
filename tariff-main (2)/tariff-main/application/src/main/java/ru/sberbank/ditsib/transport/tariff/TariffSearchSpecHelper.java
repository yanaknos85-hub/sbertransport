package ru.sberbank.ditsib.transport.tariff;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.sberbank.ditsib.transport.constants.GroupTransferClass;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.dto.TariffSearchDTO;
import ru.sberbank.ditsib.transport.tariff.dto.TransportClass;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@UtilityClass
public class TariffSearchSpecHelper {

    public Specification<BaseTariff> getSpecificationWithoutFilters(UUID organizationId,
                                                                    boolean isDataMaster) {
        return (root, query, builder) -> {
            Predicate predicate = builder.and();
            predicate = addOrganizationIdDataMasterPredicate(organizationId, isDataMaster, root, builder, predicate);
            return predicate;
        };
    }

    public Specification<BaseTariff> getSpecification(TariffSearchDTO tariffSearchDTO,
                                                      UUID organizationId,
                                                      boolean isDataMaster) {
        return (root, query, builder) -> {
            Predicate predicate = builder.and();
            if (tariffSearchDTO != null) {
                predicate = addServiceTypePredicate(tariffSearchDTO, root, builder, predicate);
                predicate = addTransportTypePredicate(tariffSearchDTO, root, builder, predicate);
                predicate = addContractNumberPredicate(tariffSearchDTO, root, builder, predicate);
                predicate = addTransportClassPredicate(tariffSearchDTO, root, builder, predicate);
                predicate = addContractPredicate(tariffSearchDTO, root, builder, predicate);
                predicate = addContractTypePredicate(tariffSearchDTO, root, builder, predicate);
                predicate = addContractorIdPredicate(tariffSearchDTO, root, builder, predicate);
                predicate = addOrganizationIdPredicate(tariffSearchDTO, root, builder, predicate);
                predicate = addOrganizationIdDataMasterPredicate(organizationId, isDataMaster, root, builder, predicate);
                predicate = addOHumanReadableIdPredicate(tariffSearchDTO, root, builder, predicate);
                predicate = addActivePredicate(tariffSearchDTO, root, builder, predicate);
                predicate = addRegionIdPredicate(tariffSearchDTO, root, builder, predicate);
                predicate = addNightTariffPredicate(tariffSearchDTO, root, builder, predicate);
            }
            return predicate;
        };
    }

    private static Predicate addNightTariffPredicate(TariffSearchDTO tariffSearchDTO, Root<BaseTariff> root, CriteriaBuilder builder, Predicate predicate) {
        if (Optional.ofNullable(tariffSearchDTO.getIsNightTariff()).orElse(false)) {
            Root<TaxiTariff> rootTaxi = builder.treat(root, TaxiTariff.class);
            predicate = builder.and(predicate, builder.equal(rootTaxi.get(TaxiTariff_.IS_NIGHT_TARIFF), true));
        }
        return predicate;
    }

    private static Predicate addRegionIdPredicate(TariffSearchDTO tariffSearchDTO, Root<BaseTariff> root, CriteriaBuilder builder, Predicate predicate) {
        if (tariffSearchDTO.getRegionId() != null) {
            predicate = builder.and(predicate, builder.equal(
                    root.get(BaseTariff_.REGION_ID), tariffSearchDTO.getRegionId()));
        }
        return predicate;
    }

    private static Predicate addActivePredicate(TariffSearchDTO tariffSearchDTO, Root<BaseTariff> root, CriteriaBuilder builder, Predicate predicate) {
        if (tariffSearchDTO.getActive() != null) {
            predicate = builder.and(predicate, builder.equal(
                    root.get(BaseTariff_.ACTIVE), tariffSearchDTO.getActive()));
        }
        return predicate;
    }

    private static Predicate addOHumanReadableIdPredicate(TariffSearchDTO tariffSearchDTO, Root<BaseTariff> root, CriteriaBuilder builder, Predicate predicate) {
        if (StringUtils.hasText(tariffSearchDTO.getHumanReadableId())) {
            predicate = builder.and(predicate, builder.like(builder.lower(
                    root.get(BaseTariff_.HUMAN_READABLE_ID)), "%" + tariffSearchDTO.getHumanReadableId().toLowerCase(Locale.ROOT) +
                    "%"));
        }
        return predicate;
    }

    private static Predicate addOrganizationIdDataMasterPredicate(UUID organizationId, boolean isDataMaster, Root<BaseTariff> root, CriteriaBuilder builder, Predicate predicate) {
        if (organizationId != null && !isDataMaster) {
            predicate = builder.and(predicate, builder.equal(root.get(BaseTariff_.ORGANIZATION).get(Organization_.ID),
                    organizationId));
        }
        return predicate;
    }

    private static Predicate addOrganizationIdPredicate(TariffSearchDTO tariffSearchDTO, Root<BaseTariff> root, CriteriaBuilder builder, Predicate predicate) {
        if (tariffSearchDTO.getOrganizationId() != null) {
            predicate = builder.and(predicate, builder.equal(root.get(BaseTariff_.ORGANIZATION).get(Organization_.ID),
                    tariffSearchDTO.getOrganizationId()));
        }
        return predicate;
    }

    private static Predicate addContractorIdPredicate(TariffSearchDTO tariffSearchDTO, Root<BaseTariff> root, CriteriaBuilder builder, Predicate predicate) {
        if (tariffSearchDTO.getContractorId() != null) {
            Root<BaseTariffWithContract> rootContract = builder.treat(root, BaseTariffWithContract.class);
            Join<BaseTariffWithContract, Contract> contractJoin = rootContract.join(BaseTariffWithContract_.CONTRACT);
            predicate = builder.and(predicate, builder.equal(contractJoin.get(Contract_.CONTRACTOR_ID),
                    tariffSearchDTO.getContractorId()));
        }
        return predicate;
    }

    private static Predicate addContractTypePredicate(TariffSearchDTO tariffSearchDTO, Root<BaseTariff> root, CriteriaBuilder builder, Predicate predicate) {
        if (tariffSearchDTO.getContractType() != null) {
            Root<BaseTariffWithContract> rootContract = builder.treat(root, BaseTariffWithContract.class);
            Join<BaseTariffWithContract, Contract> contractJoin = rootContract.join(BaseTariffWithContract_.CONTRACT);
            predicate = builder.and(predicate, builder.equal(contractJoin.get(Contract_.CONTRACT_TYPE),
                    tariffSearchDTO.getContractType()));
        }
        return predicate;
    }

    private static Predicate addContractPredicate(TariffSearchDTO tariffSearchDTO, Root<BaseTariff> root, CriteriaBuilder builder, Predicate predicate) {
        if (tariffSearchDTO.getContractId() != null) {
            Root<BaseTariffWithContract> rootContract = builder.treat(root, BaseTariffWithContract.class);
            predicate = builder.and(predicate, builder.equal(rootContract.get(BaseTariffWithContract_.CONTRACT),
                    new Contract(tariffSearchDTO.getContractId())));
        }
        return predicate;
    }

    private static Predicate addTransportClassPredicate(TariffSearchDTO tariffSearchDTO, Root<BaseTariff> root, CriteriaBuilder builder, Predicate predicate) {
        if (tariffSearchDTO.getTransportClass() != null) {
            predicate = getPredicateForTransportClass(tariffSearchDTO.getTransportClass(), root, builder, predicate);
        }
        return predicate;
    }

    private static Predicate addContractNumberPredicate(TariffSearchDTO tariffSearchDTO, Root<BaseTariff> root, CriteriaBuilder builder, Predicate predicate) {
        if (tariffSearchDTO.getContractNumber() != null) {
            Root<BaseTariffWithContract> rootContract = builder.treat(root, BaseTariffWithContract.class);
            predicate = builder.and(predicate, builder.equal(rootContract.get(BaseTariffWithContract_.CONTRACT).get(Contract_.CONTRACT_NUMBER),
                    tariffSearchDTO.getContractNumber()
            ));
        }
        return predicate;
    }

    private static Predicate addTransportTypePredicate(TariffSearchDTO tariffSearchDTO, Root<BaseTariff> root, CriteriaBuilder builder, Predicate predicate) {
        if (tariffSearchDTO.getTransportType() != null) {
            predicate = builder.and(predicate, builder.equal(root.get(BaseTariff_.TRANSPORT_TYPE),
                    tariffSearchDTO.getTransportType()));
        }
        return predicate;
    }

    private static Predicate addServiceTypePredicate(TariffSearchDTO tariffSearchDTO, Root<BaseTariff> root, CriteriaBuilder builder, Predicate predicate) {
        if (tariffSearchDTO.getServiceType() != null) {
            predicate = builder.and(predicate, builder.equal(root.get(BaseTariff_.SERVICE_TYPE),
                    tariffSearchDTO.getServiceType()));
        }
        return predicate;
    }

    private Predicate getPredicateForTransportClass(
            TransportClass transportClass, Root<BaseTariff> root, CriteriaBuilder builder, Predicate predicate
    ) {
        Predicate taxiClassPredicate = null;
        if (transportClass.getTaxiValue() != null) {
            taxiClassPredicate = getTaxiClassPredicate(transportClass, root, builder);
        }

        Predicate groupTransferPredicate = null;
        if (transportClass.getGroupTransferValue() != null) {
            groupTransferPredicate = getGroupTransferPredicate(transportClass, root, builder);

        }

        if (taxiClassPredicate != null) {
            if (groupTransferPredicate != null) {
                return builder.and(predicate, builder.or(taxiClassPredicate, groupTransferPredicate));
            } else {
                return builder.and(predicate, taxiClassPredicate);
            }
        }
        return builder.and(predicate, groupTransferPredicate);
    }

    private Predicate getTaxiClassPredicate(TransportClass transportClass, Root<BaseTariff> root, CriteriaBuilder builder) {
        return TaxiClass.getByName(transportClass.getTaxiValue()).map(taxiClass -> {
            Root<TaxiTariff> taxiTariffRoot = builder.treat(root, TaxiTariff.class);
            return builder.equal(taxiTariffRoot.get(TaxiTariff_.TAXI_CLASS), taxiClass);
        }).orElse(null);
    }

    private Predicate getGroupTransferPredicate(TransportClass transportClass, Root<BaseTariff> root, CriteriaBuilder builder) {
        return GroupTransferClass.getByName(transportClass.getGroupTransferValue()).map(mappedGroupTransfer -> {
            Root<GroupTransferTariff> groupTransferTariffRoot = builder.treat(root, GroupTransferTariff.class);
            return builder.equal(groupTransferTariffRoot.get(GroupTransferTariff_.GROUP_TRANSFER_CLASS), mappedGroupTransfer);
        }).orElse(null);
    }
}
