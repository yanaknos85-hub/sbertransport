import { CargoListItem } from '../../modules/Exchange/types';

export enum SortProperty {
  CREATION_DATE = 'CREATION_DATE',
}

export const SortPropertyRusType = {
  [SortProperty.CREATION_DATE]: {
    ASC: 'Сначала старые',
    DESC: 'Сначала новые',
  },
};

export enum ListType {
  AVAILABLE = 'available',
  NON_TERMINAL = 'non_terminal',
  TERMINAL = 'terminal',
}

export interface DesiredDateRange {
  start: string; end: string;
}

export interface SortSetting {
  directionAsc: boolean;
  property: SortProperty;
}

export interface PageSetting {
  page: number;
  size: number;
}

export interface Coordinates {
  latitude: number;
  longitude: number;
}

export interface SortSettings {
  available: SortSetting;
  progress: SortSetting;
  finished: SortSetting;
}

interface Request {
  humanReadableId: string;
  waypointType: string;
  organization: string;
  cargo: CargoListItem[];
  comment: string;
}

export interface Contacts {
  requests: Request[];
  contact: {
    phone: string;
    name: string;
  };
}

export interface WaypointExchange {
  id: string;
  type: string;
  address: {
    addressStringRepresentation: string;
    coordinates: Coordinates;
  };
  orderingIndex: number;
  contacts: Contacts[];
}
