/* eslint-disable no-use-before-define */
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

export interface ITransportType {
  id: string;
  name: string;
  rusName: string;
}

export enum TransportTypeEnum {
  TAXI = 'TAXI',
  PERSONAL = 'PERSONAL',
  PUBLIC = 'PUBLIC',
  CARSHARING = 'CARSHARING',
  DEDICATED = 'DEDICATED',
  COURIER = 'COURIER',
  INTERREGIONAL = 'INTERREGIONAL',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
  INDIVIDUAL = 'INDIVIDUAL',
  SPECIAL = 'SPECIAL',
  PRIVATE = 'PRIVATE',
  OFFICIAL = 'OFFICIAL',
}

export enum TransportTypeTitlesEnum {
  TAXI = 'Такси',
  PERSONAL = 'Личный транспорт',
  PUBLIC = 'Общественный транспорт',
  CARSHARING = 'Каршеринг',
  DEDICATED = 'Доставка сборного груза',
  COURIER = 'Курьерская доставка',
  INTERREGIONAL = 'Межрегиональная доставка',
  DOMESTIC_COURIER = 'Внутренний курьер',
  INDIVIDUAL = 'Доставка выделенным транспортом',
  SPECIAL = 'Специальный',
  PRIVATE = 'Личный',
  OFFICIAL = 'Служебный',
}

export type TransportType = keyof typeof TransportTypeEnum;

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
