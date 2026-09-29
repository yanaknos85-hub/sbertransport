import moment from 'moment/moment';
import { Organization } from 'stores/Corporate/Corporate.interface';
import { TransportType, TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import {
  RegionInfoType, Tariff, TariffJson, TariffsJournal
} from 'stores/Tariffs/Tariffs.interface';
import { TransportServiceType } from 'stores/TransportServiceTypes/TransportServiceTypes.interface';
import { Contractor } from 'stores/Contractors/Contractors.interface';
import { useMemo } from 'react';

/**
 * Подготавливает массив объектов с сервера в пригодный для отображения в журнале массив тарифов
 * @param rawTariffs - массив тарифов с сервера
 * @param organizations - организации (для получения русскоязычного имени)
 * @param transportTypes - виды транспорта (для получения русскоязычного имени)
 * @param transportServiceTypes - виды услуг
 * @param contractors - контрагенты
 * @param regionList
 */
export const processJournalTariffs = (
  rawTariffs: Tariff[],
  organizations: Organization[],
  transportTypes: TransportType[],
  transportServiceTypes: TransportServiceType[],
  contractors: Contractor[],
  regionList: RegionInfoType[]
): TariffsJournal[] => {
  const result: TariffsJournal[] = [];
  rawTariffs.forEach(tariff => {
    const regionInd: number = regionList.findIndex(r => r.id === tariff.regionId);
    const processedTariff: TariffsJournal = {
      id: tariff.id,
      active: tariff.active ? 'Активен' : 'Отключен',
      humanReadableId: tariff.humanReadableId,
      departmentHumanReadableId: tariff.departmentHumanReadableId,
      organizationId: tariff.organizationId,
      serviceType: tariff.serviceType,
      transportType: tariff.transportType,
      region: regionInd === -1 ? tariff.region : regionList[regionInd].name,
      contractorId: tariff.contractorId,
      contractNomer: tariff.contractNomer,
      isNightTariff: tariff.transportType === TransportTypes.TAXI ? (tariff.isNightTariff ? 'да' : 'нет') : '-',
    };
    const org = organizations.find(org => org.id === tariff.organizationId);
    processedTariff.organizationId = org ? org.officialName : '';

    const type = transportTypes.find(t => t.name === tariff.transportType);
    processedTariff.transportType = type ? type.rusName : '';

    const service = transportServiceTypes.find(service => service.name === tariff.serviceType);
    processedTariff.serviceType = service ? service.rusName : '';

    const contractorName = contractors.find(c => c.id === tariff.contractorId);
    processedTariff.contractorId = contractorName ? contractorName.name : '';

    result.push(processedTariff);
  });
  return result;
};

/**
 * Заполняет форму создания тарифа начальными значениями
 * @param form - форма
 * @param transportType - вид транспорта
 */
export const fillFormOnCreate = (form: any, transportType: string) => {
  switch (transportType) {
    case TransportTypes.TAXI:
      form.setFieldsValue({
        waitCostPerMinIntermediate: 1,
        suburbServiceCostPerKm: 1,
        suburbServiceCostPerMin: 1,
        costPerKmSuburb: 1,
        costPerMinSuburb: 1,
        coefWorkDayMorning: 1,
        coefWorkDayNoon: 1,
        coefWorkDayEvening: 1,
        coefWorkDayNight: 1,
        coefDayOff: 1,
        coefTraffic: 1,
        coefChildSeat: 1,
        coefPetTransport: 1,
        coefBicycle: 1,
        coefOrg: 1,
        minCancelTimeMin: 30,
        maxDiffComputedDistancePercent: 20,
        maxDiffFactDistancePercent: 20,
        maxDiffComputedCostPercent: 20,
        maxDiffContractorCostPercent: 1,
        maxDiffComputedWaitingPercent: 20,
      });
      break;
    case TransportTypes.PERSONAL:
      form.setFieldsValue({
        coefEngine1_6: 1,
        coefEngine1_6_to_2_0: 1,
        coefEngine2_0_to_2_5: 1,
        seasonalCoefficient: 1,
        coefDayOff: 1,
        coefTraffic: 1,
        coefWorkDayMorning: 1,
        coefWorkDayNoon: 1,
        coefWorkDayEvening: 1,
        coefWorkDayNight: 1,
        coefMaterialAssets: 1,
      });
      break;
    case TransportTypes.CARSHARING:
      form.setFieldsValue({
        coefWorkDayMorning: 1,
        coefWorkDayNoon: 1,
        coefWorkDayEvening: 1,
        coefWorkDayNight: 1,
        coefDayOff: 1,
        coefTraffic: 1,
        coefChildSeat: 1,
        coefPetTransport: 1,
        coefCasko: 1,
      });
      break;
    case TransportTypes.BICYCLE:
    case TransportTypes.SCOOTER:
      form.setFieldsValue({
        coefWorkDayMorning: 1,
        coefWorkDayNoon: 1,
        coefWorkDayEvening: 1,
        coefWorkDayNight: 1,
        coefDayOff: 1,
        coefInsurance: 1,
      });
      break;
    case TransportTypes.DEDICATED:
      form.setFieldsValue({
        tariffKm: 1,
        costLoader: 1,
        minTimeLoader: 1,
        minCostTimeLoader: 1,
        driverLoader: false,
        freeWaitingAmount: 0,
        waitingCostMinute: 0,
        express: 0,
        distanceIncluded: 0,
        minRideDistanceCost: 0,
        maxRouteLength: 800,
        maxWaypointCount: 20,
      });
      break;
    case TransportTypes.INDIVIDUAL:
      form.setFieldsValue({
        tariffKm: 1,
        costLoader: 1,
        minTimeLoader: 1,
        minCostTimeLoader: 1,
        driverLoader: false,
        express: 0,
        distanceIncluded: 0,
        minRideDistanceCost: 0,
        maxRouteLength: 800,
        maxWaypointCount: 20,
      });
      break;
    default:
      break;
  }
};

/**
 * Собирает объект, пригодный для отправки на сервер (создание тарифа)
 * @param data - обрабатываемый объект
 * @param id - id записи (определяет отправление на запись или на изменение)
 * @param transportType - вид транспорта
 */
export const prepareToSubmitData = (data: any, id?: string, transportType?: string): TariffJson | {} => {
  if (transportType === TransportTypes.TAXI) {
    const {
      coefWorkDayMorning,
      coefWorkDayNoon,
      coefWorkDayEvening,
      coefWorkDayNight,
      coefDayOff,
      costPerKmSuburb,
      costPerMinSuburb,
      suburbServiceCostPerKm,
      suburbServiceCostPerMin,
      costPerKmInterRegion,
      savingsDeviationPct,
      costPerMinInterRegion,
      distanceDeviationKm,
      timeDeviationMin,
      minCancelTimeMin,
      maxDiffComputedDistancePercent,
      maxDiffFactDistancePercent,
      maxDiffComputedCostPercent,
      maxDiffContractorCostPercent,
      maxDiffComputedWaitingPercent,
      rideCostPerKm,
      rideCostPerMin,
      triggerTime,
      departmentId,
      departmentHumanReadableId,
      code,
      ...other
    } = data;
    return {
      id,
      rideCostPerKm: rideCostPerKm ?? 0,
      rideCostPerMin: rideCostPerMin ?? 0,
      ...other,
      suburbTariffParams: {
        costPerKmSuburb,
        costPerMinSuburb,
        suburbServiceCostPerKm,
        suburbServiceCostPerMin,
        costPerKmInterRegion,
        costPerMinInterRegion,
      },
      coopTariffParams: {
        savingsDeviationPct,
        distanceDeviationKm,
        timeDeviationMin,
      },
      timedTariffParams: {
        coefWorkDayMorning,
        coefWorkDayNoon,
        coefWorkDayEvening,
        coefWorkDayNight,
        coefDayOff,
      },
      contractorDeviationParams: {
        maxDiffComputedDistancePercent,
        maxDiffFactDistancePercent,
        maxDiffComputedCostPercent,
        maxDiffContractorCostPercent,
        maxDiffComputedWaitingPercent,
      },
      department: {
        id: departmentId,
        humanReadableId: departmentHumanReadableId,
        code,
      },
      triggerTime,
    };
  }
  if (transportType === TransportTypes.PERSONAL) {
    const {
      coefWorkDayMorning,
      coefWorkDayNoon,
      coefWorkDayEvening,
      coefWorkDayNight,
      coefDayOff,
      costPerKmSuburb,
      costPerMinSuburb,
      suburbServiceCostPerKm,
      suburbServiceCostPerMin,
      costPerKmInterRegion,
      savingsDeviationPct,
      costPerMinInterRegion,
      distanceDeviationKm,
      timeDeviationMin,
      minCancelTimeMin,
      seasonStartEnd,
      coefEngine1_6,
      coefEngine1_6_to_2_0,
      coefEngine2_0_to_2_5,
      ...other
    } = data;

    return {
      id,
      ...other,
      engineTariffParams: {
        coefEngine1_6,
        coefEngine1_6_to_2_0,
        coefEngine2_0_to_2_5,
      },
      suburbTariffParams: {
        costPerKmSuburb,
        costPerMinSuburb,
        suburbServiceCostPerKm,
        suburbServiceCostPerMin,
        costPerKmInterRegion,
        costPerMinInterRegion,
      },
      coopTariffParams: {
        savingsDeviationPct,
        distanceDeviationKm,
        timeDeviationMin,
        minCancelTimeMin,
      },
      timedTariffParams: {
        coefWorkDayMorning,
        coefWorkDayNoon,
        coefWorkDayEvening,
        coefWorkDayNight,
        coefDayOff,
      },
      seasonStart: seasonStartEnd ? new Date(seasonStartEnd[0]).toISOString().split('T')[0] : seasonStartEnd,
      seasonEnd: seasonStartEnd ? new Date(seasonStartEnd[1]).toISOString().split('T')[0] : seasonStartEnd,
    };
  }
  if (transportType === TransportTypes.CARSHARING) {
    const {
      coefWorkDayMorning, coefWorkDayNoon, coefWorkDayEvening, coefWorkDayNight, coefDayOff, ...other
    } = data;
    return {
      id,
      ...other,
      timedTariffParams: {
        coefWorkDayMorning,
        coefWorkDayNoon,
        coefWorkDayEvening,
        coefWorkDayNight,
        coefDayOff,
      },
    };
  }
  if (transportType === TransportTypes.SCOOTER || transportType === TransportTypes.BICYCLE) {
    const {
      coefWorkDayMorning, coefWorkDayNoon, coefWorkDayEvening, coefWorkDayNight, coefDayOff, ...other
    } = data;
    return {
      id,
      ...other,
      timedTariffParams: {
        coefWorkDayMorning,
        coefWorkDayNoon,
        coefWorkDayEvening,
        coefWorkDayNight,
        coefDayOff,
      },
    };
  }
  if (transportType === TransportTypes.PUBLIC) {
    const {
      metroTicketCost = { cost: 0, enabled: false },
      trolleybusTicketCost = { cost: 0, enabled: false },
      tramTicketCost = { cost: 0, enabled: false },
      busTicketCost = { cost: 0, enabled: false },
      travelCardMetroCost = { cost: 0, enabled: false },
      travelCardTramCost = { cost: 0, enabled: false },
      travelCardBusCost = { cost: 0, enabled: false },
      travelCardTrolleybusCost = { cost: 0, enabled: false },
      travelCardAllCityTransportCost = { cost: 0, enabled: false },
      ...other
    } = data;

    return {
      ...other,
      metroTicketCost: metroTicketCost.enabled ? metroTicketCost.cost : 0,
      metroAvailability: metroTicketCost.enabled,
      tramTicketCost: tramTicketCost.enabled ? tramTicketCost.cost : 0,
      tramAvailability: tramTicketCost.enabled,
      trolleybusTicketCost: trolleybusTicketCost.enabled ? trolleybusTicketCost.cost : 0,
      trolleybusAvailability: trolleybusTicketCost.enabled,
      busTicketCost: busTicketCost.enabled ? busTicketCost.cost : 0,
      busAvailability: busTicketCost.enabled,
      travelCardMetroCost: travelCardMetroCost.enabled ? travelCardMetroCost.cost : 0,
      travelCardMetroAvailability: travelCardMetroCost.enabled,
      travelCardTramCost: travelCardTramCost.enabled ? travelCardTramCost.cost : 0,
      travelCardTramAvailability: travelCardTramCost.enabled,
      travelCardBusCost: travelCardBusCost.enabled ? travelCardBusCost.cost : 0,
      travelCardBusAvailability: travelCardBusCost.enabled,
      travelCardTrolleybusCost: travelCardTrolleybusCost.enabled ? travelCardTrolleybusCost.cost : 0,
      travelCardTrolleybusAvailability: travelCardTrolleybusCost.enabled,
      travelCardAllCityTransportCost: travelCardAllCityTransportCost.enabled ? travelCardAllCityTransportCost.cost : 0,
      travelCardAllCityTransportAvailability: travelCardAllCityTransportCost.enabled,
    };
  }
  if (transportType === TransportTypes.DEDICATED || transportType === TransportTypes.INDIVIDUAL) {
    const { organizationId, ...other } = data;
    return {
      id,
      ...other,
      distanceIncluded: data.distanceIncluded || 0,
      minRideDistanceCost: data.minRideDistanceCost || 0,
    };
  }
  return {};
};
export const MAX_TRAVEL_CARD_VALUE = 100000;
/**
 * Заполняет поля начальными значениями при открытии тарифа на редактирование
 * @param form - форма тарифа
 * @param initialValues - начальные значения
 * @param transportType - вид транспорта
 */
export const fillFormOnUpdate = (form: any, initialValues: TariffJson, transportType: string) => {
  const {
    timedTariffParams,
    suburbTariffParams,
    coopTariffParams,
    engineTariffParams,
    contractorDeviationParams,
    seasonStart,
    seasonEnd,
    ...other
  } = initialValues;
  if (transportType === TransportTypes.TAXI) {
    const taxi = {
      ...other,
      ...timedTariffParams,
      ...suburbTariffParams,
      ...engineTariffParams,
      ...coopTariffParams,
      ...contractorDeviationParams,
      departmentId: initialValues.department?.code,
    };
    form.setFieldsValue({ ...taxi });
  } else if (transportType === TransportTypes.PERSONAL) {
    const now = moment();
    const personal = {
      seasonStartEnd: [
        moment(initialValues.seasonStart).set({
          hour: now.hour(), minute: now.minute(), second: now.second(),
        }),
        moment(initialValues.seasonEnd).set({
          hour: now.hour(), minute: now.minute(), second: now.second(),
        }),
      ],
      ...other,
      ...timedTariffParams,
      ...suburbTariffParams,
      ...engineTariffParams,
      ...coopTariffParams,
    };
    form.setFieldsValue({ ...personal });
  } else if (transportType === TransportTypes.CARSHARING) {
    const carSharing = { ...other, ...timedTariffParams };
    form.setFieldsValue({ ...carSharing });
  } else if (
    transportType === TransportTypes.SCOOTER
    || transportType === TransportTypes.BICYCLE
    || transportType === TransportTypes.DEDICATED
    || transportType === TransportTypes.COURIER
    || transportType === TransportTypes.INTERREGIONAL
    || transportType === TransportTypes.DOMESTIC_COURIER
    || transportType === TransportTypes.INDIVIDUAL
  ) {
    const bicycle = { ...other, ...timedTariffParams };
    form.setFieldsValue({ ...bicycle });
  } else if (transportType === TransportTypes.PUBLIC) {
    const {
      metroTicketCost,
      metroAvailability,
      travelCardMetroCost,
      travelCardMetroAvailability,
      tramTicketCost,
      tramAvailability,
      travelCardTramCost,
      travelCardTramAvailability,
      busTicketCost,
      busAvailability,
      travelCardBusCost,
      trolleybusTicketCost,
      trolleybusAvailability,
      travelCardTrolleybusCost,
      travelCardTrolleybusAvailability,
      travelCardAllCityTransportCost,
      travelCardAllCityTransportAvailability,
      travelCardBusAvailability,
    } = other;
    form.setFieldsValue({
      ...other,
      metroTicketCost: { cost: metroTicketCost, enabled: metroAvailability },
      travelCardMetroCost: { cost: travelCardMetroCost, enabled: travelCardMetroAvailability },
      tramTicketCost: { cost: tramTicketCost, enabled: tramAvailability },
      travelCardTramCost: { cost: travelCardTramCost, enabled: travelCardTramAvailability },
      busTicketCost: { cost: busTicketCost, enabled: busAvailability },
      travelCardBusCost: { cost: travelCardBusCost, enabled: travelCardBusAvailability },
      trolleybusTicketCost: { cost: trolleybusTicketCost, enabled: trolleybusAvailability },
      travelCardTrolleybusCost: {
        cost: travelCardTrolleybusCost,
        enabled: travelCardTrolleybusAvailability,
      },
      travelCardAllCityTransportCost: {
        cost: travelCardAllCityTransportCost,
        enabled: travelCardAllCityTransportAvailability,
      },
    });
  }
};

/**
 * Вспомогательная ф-я для costRateFieldsRequired()
 * @param anotherBlockField1
 * @param anotherBlockField2
 * @param sameBlockField
 */
export const checkWhetherFieldRequiredByDependentFields = (
  anotherBlockField1?: number | string,
  anotherBlockField2?: number | string,
  sameBlockField?: number | string
) => anotherBlockField1 === '' || (anotherBlockField2 === '' && sameBlockField !== '');

/**
 * Определяет обязательность поля в зависимости от двух созависимых полей(Такси/Личный ТС)
 * @param distanceIncluded
 * @param minRideDistanceCost
 * @param timeIncluded
 * @param minRideTimeCost
 */
export const useCostRateFieldsRequired = (
  distanceIncluded?: number | string,
  minRideDistanceCost?: number | string,
  timeIncluded?: number | string,
  minRideTimeCost?: number | string
) => useMemo(
  () => ({
    distanceIncludedRequired: checkWhetherFieldRequiredByDependentFields(
      timeIncluded,
      minRideTimeCost,
      minRideDistanceCost
    ),
    minRideDistanceCostRequired: checkWhetherFieldRequiredByDependentFields(
      timeIncluded,
      minRideTimeCost,
      distanceIncluded
    ),
    timeIncludedRequired: checkWhetherFieldRequiredByDependentFields(
      distanceIncluded,
      minRideDistanceCost,
      minRideTimeCost
    ),
    minRideTimeCostRequired: checkWhetherFieldRequiredByDependentFields(
      distanceIncluded,
      minRideDistanceCost,
      timeIncluded
    ),
  }),
  [distanceIncluded, minRideDistanceCost, timeIncluded, minRideTimeCost]
);

export const getFormRule = (required: boolean, message?: string) => [{ required, message: message ?? '' }];
