import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { ITaxi, TaxiClassEnum, TaxiClassTitlesEnum } from 'stores/Trip/Trip.interface';

const TaxiClassesConfig: Record<string, ITaxi> = {
  [TaxiClassEnum.ECONOMY]: {
    name: TaxiClassTitlesEnum.ECONOMY,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.ECONOMY,
    waitingTime: 2,
  },
  [TaxiClassEnum.COMFORT]: {
    name: TaxiClassTitlesEnum.COMFORT,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.COMFORT,
    waitingTime: 5,
  },
  [TaxiClassEnum.COMFORT_PLUS]: {
    name: TaxiClassTitlesEnum.COMFORT_PLUS,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.COMFORT_PLUS,
    waitingTime: 5,
  },
  [TaxiClassEnum.BUSINESS]: {
    name: TaxiClassTitlesEnum.BUSINESS,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.BUSINESS,
    waitingTime: 7,
  },
  [TaxiClassEnum.OFFICIAL]: {
    name: TaxiClassTitlesEnum.OFFICIAL,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.OFFICIAL,
    waitingTime: 5,
  },
};

const TransportTypesConfig: Record<string, ITaxi> = {
  [TaxiClassEnum.ECONOMY]: {
    name: TaxiClassTitlesEnum.ECONOMY,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.ECONOMY,
    waitingTime: 30,
  },
  [TaxiClassEnum.COMFORT]: {
    name: TaxiClassTitlesEnum.COMFORT,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.COMFORT,
    waitingTime: 30,
  },
  [TaxiClassEnum.COMFORT_PLUS]: {
    name: TaxiClassTitlesEnum.COMFORT_PLUS,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.COMFORT_PLUS,
    waitingTime: 30,
  },
  [TaxiClassEnum.BUSINESS]: {
    name: TaxiClassTitlesEnum.BUSINESS,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.BUSINESS,
    waitingTime: 30,
  },
  [TaxiClassEnum.YANDEX_ECONOMY]: {
    name: TaxiClassTitlesEnum.ECONOMY,
    transportType: TransportTypeEnum.YANDEX,
    taxiClass: TaxiClassEnum.YANDEX_ECONOMY,
    waitingTime: 10,
  },
  [TaxiClassEnum.YANDEX_COMFORT]: {
    name: TaxiClassTitlesEnum.COMFORT,
    transportType: TransportTypeEnum.YANDEX,
    taxiClass: TaxiClassEnum.YANDEX_COMFORT,
    waitingTime: 10,
  },
  [TaxiClassEnum.PERSONAL]: {
    name: TaxiClassTitlesEnum.PERSONAL,
    transportType: TransportTypeEnum.PERSONAL,
    taxiClass: TaxiClassEnum.PERSONAL,
    waitingTime: 25,
  },
  [TaxiClassEnum.PUBLIC]: {
    name: TaxiClassTitlesEnum.PUBLIC,
    transportType: TransportTypeEnum.PUBLIC,
    taxiClass: undefined,
    waitingTime: 90,
  },
  [TaxiClassEnum.CARSHARING]: {
    name: TaxiClassTitlesEnum.CARSHARING,
    transportType: TransportTypeEnum.CARSHARING,
    taxiClass: TaxiClassEnum.CARSHARING,
    waitingTime: 4,
  },
  [TaxiClassEnum.OFFICIAL]: {
    name: TaxiClassTitlesEnum.OFFICIAL,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.OFFICIAL,
    waitingTime: 30,
  },
  [TaxiClassEnum.BICYCLE]: {
    name: TaxiClassTitlesEnum.BICYCLE,
    transportType: TransportTypeEnum.BICYCLE,
    taxiClass: undefined,
    waitingTime: 2,
  },
  [TaxiClassEnum.SCOOTER]: {
    name: TaxiClassTitlesEnum.SCOOTER,
    transportType: TransportTypeEnum.SCOOTER,
    taxiClass: undefined,
    waitingTime: 0,
  },
  [TaxiClassEnum.VIP_BUS]: {
    name: TaxiClassTitlesEnum.VIP_BUS,
    transportType: TransportTypeEnum.BUS,
    taxiClass: TaxiClassEnum.VIP_BUS,
    waitingTime: 0,
    maxPassengers: 9,
  },
  [TaxiClassEnum.SMALL_BUS]: {
    name: TaxiClassTitlesEnum.SMALL_BUS,
    transportType: TransportTypeEnum.BUS,
    taxiClass: TaxiClassEnum.SMALL_BUS,
    waitingTime: 0,
    maxPassengers: 21,
  },
  [TaxiClassEnum.MIDDLE_BUS]: {
    name: TaxiClassTitlesEnum.MIDDLE_BUS,
    transportType: TransportTypeEnum.BUS,
    taxiClass: TaxiClassEnum.MIDDLE_BUS,
    waitingTime: 0,
    maxPassengers: 41,
  },
  [TaxiClassEnum.LARGE_BUS]: {
    name: TaxiClassTitlesEnum.LARGE_BUS,
    transportType: TransportTypeEnum.BUS,
    taxiClass: TaxiClassEnum.LARGE_BUS,
    waitingTime: 0,
    maxPassengers: 55,
  },
  [TaxiClassEnum.GROUP_TRANSFER]: {
    name: TaxiClassTitlesEnum.ECONOMY,
    transportType: TransportTypeEnum.GROUP_TRANSFER,
    taxiClass: TaxiClassEnum.TRANSFER,
    waitingTime: 0,
  },
  [TaxiClassEnum.TRANSFER_COMFORT]: {
    name: TaxiClassTitlesEnum.COMFORT,
    transportType: TransportTypeEnum.GROUP_TRANSFER,
    taxiClass: TaxiClassEnum.TRANSFER_COMFORT,
    waitingTime: 0,
  },
  [TaxiClassEnum.TRANSFER_COMFORT_PLUS]: {
    name: TaxiClassTitlesEnum.COMFORT_PLUS,
    transportType: TransportTypeEnum.GROUP_TRANSFER,
    taxiClass: TaxiClassEnum.TRANSFER_COMFORT_PLUS,
    waitingTime: 0,
  },
  [TaxiClassEnum.TRANSFER_BUSINESS]: {
    name: TaxiClassTitlesEnum.BUSINESS,
    transportType: TransportTypeEnum.GROUP_TRANSFER,
    taxiClass: TaxiClassEnum.TRANSFER_BUSINESS,
    waitingTime: 0,
  },
  [TaxiClassEnum.TRANSFER_VIP]: {
    name: TaxiClassTitlesEnum.VIP,
    transportType: TransportTypeEnum.GROUP_TRANSFER,
    taxiClass: TaxiClassEnum.TRANSFER_VIP,
    waitingTime: 0,
  },
  [TaxiClassEnum.TRANSFER_CAR_CHOICE]: {
    name: TaxiClassTitlesEnum.CHOOSING_CAR_TRANSFER,
    transportType: TransportTypeEnum.GROUP_TRANSFER,
    taxiClass: TaxiClassEnum.TRANSFER_CAR_CHOICE,
    waitingTime: 0,
  },
};

const ExternalClassesConfig: Record<string, ITaxi> = {
  [TaxiClassEnum.YANDEX]: {
    name: TaxiClassTitlesEnum.YANDEX,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.YANDEX,
    waitingTime: 10,
  },
  [TaxiClassEnum.CITYMOBIL]: {
    name: TaxiClassTitlesEnum.CITYMOBIL,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.CITYMOBIL,
    waitingTime: 5,
  },
  [TaxiClassEnum.UBER]: {
    name: TaxiClassTitlesEnum.UBER,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.UBER,
    waitingTime: 6,
  },
};

export const busClasses = [
  TaxiClassEnum.VIP_BUS,
  TaxiClassEnum.SMALL_BUS,
  TaxiClassEnum.MIDDLE_BUS,
  TaxiClassEnum.LARGE_BUS,
];

// stub for all buses
export const busConfig: ITaxi = {
  name: TaxiClassTitlesEnum.BUS,
  transportType: TransportTypeEnum.BUS,
  taxiClass: TaxiClassEnum.BUS,
  waitingTime: 0,
};
export const busIdStub = 'busIdStub';

export { TaxiClassesConfig, TransportTypesConfig, ExternalClassesConfig };
