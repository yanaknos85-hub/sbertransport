export interface ITransportTypesStore {
  transportTypes: TransportTypesModel[];
  activeTransportType?: TransportType;
  availableTransportTypes: TransportTypesModel[];
  setActiveTransportType(type: TransportType): void;
  rusNamesByTransportType: Record<string, string>;
  clearActiveTransportType(): void;
  initStore(): void;
}

export interface ITransportTypesService {
  getTransportTypes(): Promise<ITransportType[]>;
  getAvailableTransportTypes(orgId: string): Promise<ITransportType[]>;
}

export interface ITransportType {
  id: string;
  name: string;
  rusName: string;
}
/* TODO удалить лишние типы транспорта */
export enum TransportTypeEnum {
  TAXI = 'TAXI',
  PUBLIC = 'PUBLIC',
  PERSONAL = 'PERSONAL',
  CARSHARING = 'CARSHARING',
  BICYCLE = 'BICYCLE',
  WALK = 'WALK',
  SCOOTER = 'SCOOTER',
  DEDICATED = 'DEDICATED',
  COURIER = 'COURIER',
  INTERREGIONAL = 'INTERREGIONAL',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
  INDIVIDUAL = 'INDIVIDUAL',
}

export enum TransportTypeTitlesEnum {
  TAXI = 'Такси',
  PUBLIC = 'Общественный',
  PERSONAL = 'Личный',
  CARSHARING = 'Каршеринг',
  BICYCLE = 'Велосипед',
  WALK = 'Пешком',
  SCOOTER = 'Самокат',
  DEDICATED = 'Доставка сборного груза',
  COURIER = 'Курьер',
  INTERREGIONAL = 'Межрегиональная',
  DOMESTIC_COURIER = 'Внутренний курьер',
  INDIVIDUAL = 'Доставка выделенным транспортом',
}

export enum TransportCompensations {
  CITY_TRIP_COMPENSATION = 'CITY_TRIP_COMPENSATION',
  SUBURB_TRIP_COMPENSATION = 'SUBURB_TRIP_COMPENSATION',
  TRAVEL_CARD_COMPENSATION = 'TRAVEL_CARD_COMPENSATION',
}

export enum TransportCompensationsTitles {
  CITY_TRIP_COMPENSATION = 'Компенсация поездки по городу',
  SUBURB_TRIP_COMPENSATION = 'Компенсация междугородних поездок',
  TRAVEL_CARD_COMPENSATION = 'Компенсация проездного документа',
}

export enum PublicTransportTypeEnum {
  METRO = 'METRO',
  TRAM = 'TRAM',
  TROLLEYBUS = 'TROLLEYBUS',
  BUS = 'BUS',

  CITY_BUS = 'CITY_BUS',
  CITY_TRAIN = 'CITY_TRAIN',
  CITY_TROLLEYBUS = 'CITY_TROLLEYBUS',
  CITY_TRAM = 'CITY_TRAM',
  CITY_METRO = 'CITY_METRO',

  SUBURB_BUS = 'SUBURB_BUS',
  SUBURB_TRAIN = 'SUBURB_TRAIN',
  SUBURB_FERRY_CROSSING = 'SUBURB_FERRY_CROSSING',
  SUBURB_TROLLEYBUS = 'SUBURB_TROLLEYBUS',

  TRAVEL_CARD_BUS = 'TRAVEL_CARD_BUS',
  TRAVEL_CARD_TRAIN = 'TRAVEL_CARD_TRAIN',
  TRAVEL_CARD_TROLLEYBUS = 'TRAVEL_CARD_TROLLEYBUS',
  TRAVEL_CARD_TRAM = 'TRAVEL_CARD_TRAM',
  TRAVEL_CARD_METRO = 'TRAVEL_CARD_METRO',
  TRAVEL_CARD_ALL_CITY_TRANSPORT = 'TRAVEL_CARD_ALL_CITY_TRANSPORT',
}
export enum PublicCityTypeEnum {
  CITY_BUS = 'CITY_BUS',
  CITY_TRAIN = 'CITY_TRAIN',
  CITY_TROLLEYBUS = 'CITY_TROLLEYBUS',
  CITY_TRAM = 'CITY_TRAM',
  CITY_METRO = 'CITY_METRO',
}
export type PublicCityType = keyof typeof PublicCityTypeEnum;

export type TransportType = keyof typeof TransportTypeEnum;

export interface PublicInfoCard {
  compensationType: string;
  cost: number;
  publicTransportType: string;
  quantity: number;
  sum: number;
  ticketCount: number;
}
export class TransportTypesModel implements ITransportType {
  name: string;

  id: string;

  rusName: string;

  constructor(transportType: ITransportType) {
    this.name = transportType.name;
    this.id = transportType.id;
    this.rusName = transportType.rusName;
  }
}
