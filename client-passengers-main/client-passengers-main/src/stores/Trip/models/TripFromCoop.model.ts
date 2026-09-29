import {
  IRequestsSharedModal,
  TEmployeeWithoutOrg,
  TTripFromCoop,
  TTripFromCoopStops,
  TTripKpi
} from '../Trip.interface';

export class TripFromCoopModel implements TTripFromCoop {
  magentaId: number;

  passengers: number;

  employeePassengers: TEmployeeWithoutOrg[];

  tariffId: string;

  active: boolean;

  stops: TTripFromCoopStops[];

  kpi: TTripKpi;

  requests: IRequestsSharedModal[] = [];

  constructor(trip: TTripFromCoop) {
    this.magentaId = trip.magentaId;
    this.passengers = trip.passengers;
    this.employeePassengers = trip.employeePassengers;
    this.tariffId = trip.tariffId;
    this.active = trip.active;
    this.stops = trip.stops;
    this.kpi = trip.kpi;
  }
}
