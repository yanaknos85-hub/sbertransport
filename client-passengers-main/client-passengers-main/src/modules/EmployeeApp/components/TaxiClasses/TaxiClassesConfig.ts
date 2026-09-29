
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
  [TaxiClassEnum.PERSONAL]: {
    name: TaxiClassTitlesEnum.PERSONAL,
    transportType: TransportTypeEnum.PERSONAL,
    taxiClass: TaxiClassEnum.PERSONAL,
    waitingTime: 25,
  },
  [TaxiClassEnum.PUBLIC]: {
    name: TaxiClassTitlesEnum.PUBLIC,
    transportType: TransportTypeEnum.PUBLIC,
    taxiClass: TaxiClassEnum.PUBLIC,
    waitingTime: 90,
  },
  [TaxiClassEnum.CARSHARING]: {
    name: TaxiClassTitlesEnum.CARSHARING,
    transportType: TransportTypeEnum.CARSHARING,
    taxiClass: TaxiClassEnum.CARSHARING,
    waitingTime: 4,
  },
  [TaxiClassEnum.BICYCLE]: {
    name: TaxiClassTitlesEnum.BICYCLE,
    transportType: TransportTypeEnum.BICYCLE,
    taxiClass: TaxiClassEnum.BICYCLE,
    waitingTime: 2,
  },
  [TaxiClassEnum.SCOOTER]: {
    name: TaxiClassTitlesEnum.SCOOTER,
    transportType: TransportTypeEnum.SCOOTER,
    taxiClass: TaxiClassEnum.SCOOTER,
    waitingTime: 0,
  },
  [TaxiClassEnum.VIP_BUS]: {
    name: TaxiClassTitlesEnum.VIP_BUS,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.VIP_BUS,
    waitingTime: 0,
    maxPassengers: 9,
  },
  [TaxiClassEnum.SMALL_BUS]: {
    name: TaxiClassTitlesEnum.SMALL_BUS,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.SMALL_BUS,
    waitingTime: 0,
    maxPassengers: 21,
  },
  [TaxiClassEnum.MIDDLE_BUS]: {
    name: TaxiClassTitlesEnum.MIDDLE_BUS,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.MIDDLE_BUS,
    waitingTime: 0,
    maxPassengers: 41,
  },
  [TaxiClassEnum.LARGE_BUS]: {
    name: TaxiClassTitlesEnum.LARGE_BUS,
    transportType: TransportTypeEnum.TAXI,
    taxiClass: TaxiClassEnum.LARGE_BUS,
    waitingTime: 0,
    maxPassengers: 55,
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
  transportType: TransportTypeEnum.TAXI,
  taxiClass: TaxiClassEnum.BUS,
  waitingTime: 0,
};
export const busIdStub = 'busIdStub';

export { TaxiClassesConfig, TransportTypesConfig, ExternalClassesConfig };
