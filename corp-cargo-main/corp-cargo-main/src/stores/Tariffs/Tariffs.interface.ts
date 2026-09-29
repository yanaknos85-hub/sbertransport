import * as t from 'io-ts';
import { createPagination } from 'stores/Pagination/Pagination.interface';
import * as tt from 'utils/io-ts';

export const Tariff = t.intersection([
  t.type({
    id: t.string,
    active: t.boolean,
    humanReadableId: t.string,
    organizationId: t.union([tt.nullable(t.string), t.undefined]),
    serviceType: t.string,
    transportType: t.string,
  }),
  t.partial({
    region: t.string,
    regionId: t.string,
    contractorId: t.string,
    contractNomer: t.string,
    departmentHumanReadableId: t.string,
    code: t.string,
    isNightTariff: t.boolean,
  }),
]);

export const TariffsJournal = t.intersection([
  t.type({
    id: t.string,
    active: t.string,
    humanReadableId: t.string,
    organizationId: t.union([tt.nullable(t.string), t.undefined]),
    serviceType: t.string,
    transportType: t.string,
  }),
  t.partial({
    region: t.string,
    regionId: t.string,
    contractorId: t.string,
    contractNomer: t.string,
    departmentHumanReadableId: t.string,
    code: t.string,
    isNightTariff: t.string,
  }),
]);

export const suburbTariffParams = t.partial({
  costPerKmSuburb: tt.money,
  costPerMinSuburb: tt.money,
  suburbServiceCostPerKm: tt.money,
  suburbServiceCostPerMin: tt.money,
  costPerKmInterRegion: tt.money,
  costPerMinInterRegion: tt.money,
});

export const coopTariffParams = t.type({
  savingsDeviationPct: t.number,
  distanceDeviationKm: t.number,
  timeDeviationMin: t.number,
  minCancelTimeMin: t.number,
});

export const timedTariffParams = t.type({
  coefWorkDayMorning: t.number,
  coefWorkDayNoon: t.number,
  coefWorkDayEvening: t.number,
  coefWorkDayNight: t.number,
  coefDayOff: t.number,
});
// Todo: Изменю name когда Андрей вольет изменение
export const RegionInfoType = t.intersection([
  t.type({
    id: t.string, name: t.string, code: t.union([t.string, t.number]),
  }),
  t.partial({
    parent_id: tt.uuid,
  }),
]);

export const engineTariffParams = t.type({
  coefEngine1_6: t.number,
  coefEngine1_6_to_2_0: t.number,
  coefEngine2_0_to_2_5: t.number,
});

export const contractorDeviationParams = t.type({
  maxDiffComputedDistancePercent: t.number,
  maxDiffFactDistancePercent: t.number,
  maxDiffComputedCostPercent: t.number,
  maxDiffContractorCostPercent: t.number,
  maxDiffComputedWaitingPercent: t.number,
});

export const Department = t.partial({
  code: t.string,
  id: tt.uuid,
  humanReadableId: t.string,
});

export const TariffJson = t.intersection([
  t.type({
    serviceType: t.string,
    organizationId: t.union([t.string, t.undefined]),
    transportType: t.string,
  }),
  t.partial({
    id: t.string,
    humanReadableId: t.string,
    contractId: t.string,
    contractNumber: t.string,
    region: t.string,
    regionId: t.array(t.string),
    taxiClass: t.string,
    contractorId: t.string,
    rideCostPerKm: tt.money,
    distanceIncluded: t.number,
    minRideDistanceCost: tt.money,
    autoPlanning: t.boolean,
    rideCostPerMin: tt.money,
    timeIncluded: t.number,
    minRideTimeCost: tt.money,
    waitCostPerMin: tt.money,
    waitCostPerMinIntermediate: tt.money,
    freeWaitingTime: t.number,
    coefTraffic: t.number,
    coefChildSeat: t.number,
    coefPetTransport: t.number,
    coefBicycle: t.number,
    coefOrg: t.number,
    coefCasko: t.number,
    coefMaterialAssets: t.number,
    coefPassenger: t.number,
    seasonalCoefficient: t.number,
    seasonStart: t.string,
    seasonEnd: t.string,
    bookingCost: tt.money,
    coefInsurance: t.number,
    metroAvailability: t.boolean,
    metroTicketCost: tt.money,
    tramAvailability: t.boolean,
    tramTicketCost: tt.money,
    trolleybusTicketCost: tt.money,
    trolleybusAvailability: t.boolean,
    busTicketCost: tt.money,
    busAvailability: t.boolean,
    travelCardMetroAvailability: t.boolean,
    travelCardMetroCost: tt.money,
    travelCardTramAvailability: t.boolean,
    travelCardTramCost: tt.money,
    travelCardBusAvailability: t.boolean,
    travelCardBusCost: tt.money,
    travelCardTrolleybusCost: tt.money,
    travelCardTrolleybusAvailability: t.boolean,
    travelCardAllCityTransportCost: tt.money,
    travelCardAllCityTransportAvailability: t.boolean,
    suburbTariffParams,
    coopTariffParams,
    timedTariffParams,
    engineTariffParams,
    contractorDeviationParams,
    costLoader: tt.money,
    minTimeLoader: t.number,
    minCostTimeLoader: tt.money,
    driverLoader: t.boolean,
    freeWaitingAmount: t.number,
    waitingCostMinute: t.number,
    express: tt.money,
    tariffKm: tt.money,
    trustIdx: t.number,
    department: Department,
    code: t.string,
    departmentId: tt.uuid,
    departmentHumanReadableId: t.string,
    calculationType: t.string,
  }),
]);

export const TariffFilter = t.intersection([
  t.type({
    page: t.type({
      pageNumber: t.number,
      pageSize: t.number,
    }),
  }),
  t.partial({
    transportType: t.string,
    serviceType: t.string,
    organizationId: tt.uuid,
    contractorId: tt.uuid,
    humanReadableId: t.string,
    regionId: t.string,
    status: t.string,
    isNightTariff: t.string,
    active: t.union([t.boolean, t.string, t.number]), // TODO: где-то используется тип string | number, разобраться
  }),
]);

export const Pack = t.type({
  id: t.string,
  name: t.string,
  unit: t.string,
});

export const PackDetails = t.type({
  pack: Pack,
  cost: t.number,
});

export const PackData = t.type({
  content: t.array(Pack),
});

export const TariffPackFilter = t.intersection([
  t.type({
    pageSetting: t.type({
      page: t.number,
      size: t.number,
    }),
  }),
  t.partial({
    sortSetting: t.type({
      directionAsc: t.boolean,
      property: t.string,
    }),
  }),
]);

export const CargoPackPost = t.type({
  contractId: t.string,
  contractorId: t.string,
  packDetails: PackDetails,
  active: t.boolean,
});

export const CargoPackPut = t.type({
  id: t.string,
  contractId: t.string,
  contractNumber: t.string,
  contractorId: t.string,
  packDetails: t.type({
    pack: Pack,
    cost: t.number,
  }),
  active: t.boolean,
});

export const TariffPack = t.intersection([
  t.type({
    id: t.string,
    contractorId: t.string,
    contractId: t.string,
    packDetails: t.array(t.type({
      id: t.string, name: t.string, unit: t.string, cost: t.number,
    })),
    active: t.boolean,
  }),
  t.partial({
    contractNumber: t.string,
    empty: t.boolean,
  }),
]);

export const TariffPackCreate = t.intersection([
  t.type({
    contractorId: t.string,
    contractId: t.string,
    pack: t.type({
      id: t.string,
    }),
    cost: t.number,
    active: t.boolean,
  }),
  t.partial({
    contractNumber: t.string,
  }),
]);

export const TariffPackInfo = t.type({
  content: t.array(TariffPack),
  size: t.number,
  totalElements: t.number,
  totalPages: t.number,
});

export const TariffsInfo = t.type({
  content: t.array(Tariff),
  size: t.number,
  totalElements: t.number,
  totalPages: t.number,
});
export const AutoGuide = t.intersection([
  t.type({
    id: t.string,
    name: t.string,
    volume: t.number,
    capacity: t.type({
      id: t.string,
      capacity: t.number,
    }),
  }),
  t.partial({
    length: t.number,
    width: t.number,
    height: t.number,
  }),
]);

export const TariffsSearchResponse = createPagination(Tariff);

export type TariffsInfo = t.TypeOf<typeof TariffsInfo>;
export type Tariff = t.TypeOf<typeof Tariff>;
export type TariffsJournal = t.TypeOf<typeof TariffsJournal>;
export type TariffJson = t.TypeOf<typeof TariffJson>;
export type TariffFilter = t.TypeOf<typeof TariffFilter>;
export type RegionInfoType = t.TypeOf<typeof RegionInfoType>;
export type SuburbTariffParams = t.TypeOf<typeof suburbTariffParams>;
export type CoopTariffParams = t.TypeOf<typeof coopTariffParams>;
export type AutoGuide = t.TypeOf<typeof AutoGuide>;
export type Department = t.TypeOf<typeof Department>;
export type PackData = t.TypeOf<typeof PackData>;
export type TariffPackInfo = t.TypeOf<typeof TariffPackInfo>;
export type TariffPack = t.TypeOf<typeof TariffPack>;
export type PackDetails = t.TypeOf<typeof PackDetails>;
export type CargoPackPut = t.TypeOf<typeof CargoPackPut>;
export type CargoPackPost = t.TypeOf<typeof CargoPackPost>;
export type TariffsSearchResponse = t.TypeOf<typeof TariffsSearchResponse>;
