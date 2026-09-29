import { getTimeZone } from '../../../modules/EmployeeApp/components/CreateTripRequest/utils/utils';
import { EmployeeModel } from '@sber-sbertransport/mf-core';
import moment from 'moment';

import { DATE_FORMAT } from 'constants/constants.app';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import {
  IRequestsModal, SharedTransportType, TaxiClassEnum, TTripCoop, TTripKpi, TTripStops
} from '../Trip.interface';

export class TripSuitableModel implements TTripCoop {
  id: string;

  employeePassengers: EmployeeModel[];

  passengers: number;

  kpi: TTripKpi;

  newOrdersIds: string[];

  stops: TTripStops[];

  isPersonal: boolean;

  type: TransportTypeEnum;

  taxiClass: TaxiClassEnum;

  readableTimeStops: TTripStops[];

  transportType: SharedTransportType;

  timeZone: string;

  requests: IRequestsModal[] = [];

  constructor(
    trip: TTripCoop & { type: TransportTypeEnum; transportType: SharedTransportType; taxiClass: TaxiClassEnum }
  ) {
    this.id = trip.id;
    this.employeePassengers = trip.employeePassengers.map(emp => new EmployeeModel(emp));
    this.passengers = trip.passengers;
    this.kpi = trip.kpi;
    this.newOrdersIds = trip.newOrdersIds;
    this.stops = trip.stops;
    this.readableTimeStops = this.stopsWithTimeReadable;
    this.isPersonal = false;
    this.type = trip.type;
    this.taxiClass = trip.taxiClass;
    this.transportType = trip.transportType;
    this.timeZone = getTimeZone();
  }

  get stopsWithTimeReadable(): TTripStops[] {
    return this.stops.map(stop => ({
      ...stop,
      endTime: moment(stop.endTime).format(DATE_FORMAT.MONTH_NAME_WITH_TIME),
      startTime: moment(stop.startTime).format(DATE_FORMAT.MONTH_NAME_WITH_TIME),
    }));
  }

  setIsPersonalTrue(): void {
    this.isPersonal = true;
  }
}
