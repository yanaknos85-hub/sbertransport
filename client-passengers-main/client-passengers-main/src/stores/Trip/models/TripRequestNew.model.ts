
import { EmployeeModel, Employee } from '@sber-sbertransport/mf-core';
import { plainToNew } from 'utils';

import { TTripRequestStatuses } from 'modules/EmployeeApp/TripRequestStatuses.constants';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import { RouteModel } from 'shared/models/geo/Route.model';
import { ApprovalStateStatuses } from 'shared/models/types';

import { TaxiClassEnum, TripPurpose, TTripRequestNew } from '../Trip.interface';

export class TripRequestNewModel implements TTripRequestNew {
  author = new EmployeeModel();

  passenger = new EmployeeModel();

  transportType?: TransportTypeEnum = TransportTypeEnum.TAXI;

  expected = new RouteModel();

  purpose: TripPurpose = {
    id: '8fb53316-1535-41a4-83d1-e39f00771a7a',
    label: '',
  };

  passengerCount?: number;

  status?: TTripRequestStatuses;

  desiredDate?: number;

  creationTime?: string | number;

  approvedBy?: Employee | EmployeeModel;

  taxiClass?: TaxiClassEnum;

  tariffId?: string;

  approvalState?: ApprovalStateStatuses;

  coopTrip?: boolean;

  commentForDriver?: string;

  commentForPurpose?: string;

  constructor(trip: TTripRequestNew) {
    if (trip) {
      this.author = new EmployeeModel(trip.author);
      this.passenger = new EmployeeModel(trip.passenger);
      this.transportType = trip.transportType;
      this.expected = plainToNew(RouteModel, trip?.expected) ?? new RouteModel();
      this.purpose = trip?.purpose;

      this.taxiClass = trip?.taxiClass;
      this.passengerCount = trip?.passengerCount;
      this.tariffId = trip?.tariffId;
      this.desiredDate = trip?.desiredDate;
      this.creationTime = trip?.creationTime;
      this.approvedBy = new EmployeeModel(trip?.approvedBy);
      this.approvalState = trip?.approvalState;
      this.status = trip?.status;
    }
  }
}
