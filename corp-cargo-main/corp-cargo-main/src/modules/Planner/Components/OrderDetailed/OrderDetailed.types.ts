import { CalculatedTariffType, OrderWaypointType, Segment } from "../../types";

// TODO Данные будут изменены, после получения корректного DTO от бэка
 type Author = {
  id: string;
  firstName: string;
  lastName: string;
  patronymic: string;
}

type Cargo = {
  position: number;
  cargoName: string;
  cargoType: string;
  cargoCategory: string;
  length: number;
  width: number;
  height: number;
  volume: number;
  weight: number;
  occupiedPlacesCount: number;
  fragile: boolean;
  needPackage: boolean;
  packageCount: number;
  cost: number;
  distance: number;
}

// type Waypoint = {
//   type: string;
//   orderingIndex: number;
//   country: string;
//   region: string;
//   city: string;
//   street: string;
//   house: string;
//   latitude: number;
//   longitude: number;
// }

type CargoDimensions = {
  length: number;
  width: number;
  height: number;
  volume: number;
  weight: number;
  occupiedPlacesCount: number;
}

export type OrderData = {
  id: string;
  humanReadableId: string;
  author: Author;
  transportType: string;
  transportTypeRus: string;
  totalSizes: CargoDimensions;
  calculatedTariff: CalculatedTariffType;
  express: boolean;
  desiredDate: number;
  listCargo: Cargo[];
  status: string;
  loaders: number;
  waypoints: OrderWaypointType[];
  segments: Segment[]; 
}