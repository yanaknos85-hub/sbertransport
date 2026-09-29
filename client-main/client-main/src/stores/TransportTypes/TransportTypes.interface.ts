export interface ITransportTypesStore {
  transportTypes: TransportTypesModel[];
  activeTransportType?: TransportType;
  availableTransportTypes: TransportTypesModel[];
  availableTransportTypesByService: TransportTypesModel[];
  setActiveTransportType(type: TransportType): void;
  rusNamesByTransportType: Record<string, string>;
  clearActiveTransportType(): void;
  getAvailableTransportTypes(): void;
  getAvailableTransportTypesByService(serviceType: string): void;
  initStore(): void;
}

export interface ITransportTypesService {
  getTransportTypes(): Promise<ITransportType[]>;
  getAvailableTransportTypes(orgId: string): Promise<ITransportType[]>;
  getAvailableTransportTypesByService(serviceType: string, orgId: string): Promise<ITransportType[]>;
}

export interface ITransportType {
  id: string;
  name: string;
  rusName: string;
}

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
  GROUP_TRANSFER = 'GROUP_TRANSFER',
}

export enum TransportTypeTitlesEnum {
  TAXI = 'Такси',
  PUBLIC = 'Общественный',
  PERSONAL = 'Личный',
  CARSHARING = 'Каршеринг',
  BICYCLE = 'Велосипед',
  WALK = 'Пешком',
  SCOOTER = 'Самокат',
  DEDICATED = 'Доставка',
  COURIER = 'Курьер',
  INTERREGIONAL = 'Межрегиональная',
  GROUP_TRANSFER = 'Трансфер',
}

export enum TransportTypeLongTitlesEnum {
  TAXI = 'Такси',
  PUBLIC = 'Общественный транспорт',
  PERSONAL = 'Личный транспорт',
  CARSHARING = 'Каршеринг',
  BICYCLE = 'Велосипед',
  WALK = 'Пешком',
  SCOOTER = 'Самокат',
  DEDICATED = 'Доставка сборного груза',
  INDIVIDUAL = 'Доставка выделенным транспортом',
  COURIER = 'Курьерская доставка',
  INTERREGIONAL = 'Межрегиональная доставка',
  DOMESTIC_COURIER = 'Внутренний курьер',
  GROUP_TRANSFER = 'Групповой трансфер',
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

export enum TransportTypesPassenger {
  PUBLIC = 'PUBLIC',
  PERSONAL = 'PERSONAL',
  TAXI = 'TAXI',
  CARSHARING = 'CARSHARING',
}

export enum TransportTypesCargo {
  COURIER = 'COURIER',
  DEDICATED = 'DEDICATED',
  INTERREGIONAL = 'INTERREGIONAL',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
  INDIVIDUAL = 'INDIVIDUAL',
}

export const TransportTypeCargoTitles: Record<TransportTypesCargo, string> = {
  [TransportTypesCargo.COURIER]: 'Курьер',
  [TransportTypesCargo.DEDICATED]: 'Доставка сборного груза',
  [TransportTypesCargo.INTERREGIONAL]: 'Межрегиональная доставка',
  [TransportTypesCargo.DOMESTIC_COURIER]: 'Внутренний курьер',
  [TransportTypesCargo.INDIVIDUAL]: 'Доставка выделенным транспортом',
};

export enum FleetManagementTypes {
  OFFICIAL = 'OFFICIAL',
  PRIVATE = 'PRIVATE',
  SPECIAL = 'SPECIAL',
}

export const FleetManagementTitles: Record<FleetManagementTypes, string> = {
  [FleetManagementTypes.OFFICIAL]: 'Служебный',
  [FleetManagementTypes.PRIVATE]: 'Персональный',
  [FleetManagementTypes.SPECIAL]: 'Специальный',
};
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

export const TransportTypeTitles = {
  ...TransportTypeTitlesEnum,
  ...TransportTypeCargoTitles,
  ...FleetManagementTitles,
  ...TransportTypeLongTitlesEnum,
};
