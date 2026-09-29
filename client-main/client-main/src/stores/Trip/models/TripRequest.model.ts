import { EmployeeModel, PersonalCar } from '@sber-sbertransport/mf-core';
import { action } from 'mobx';
import moment from 'moment';
import {
  TTripRequestStatuses,
  TripStatusesCancellable,
  TripStatusesChangeable,
  TripStatusesFinal
} from 'constants/TripRequestStatuses.constants';
import { RouteModel } from 'shared/models/geo/Route.model';
import { RequestBaseModel } from 'shared/models/RequestBase.model';
import { ApprovalStateStatuses } from 'shared/models/types';
import { LimitSharing } from 'stores/Limits/Limit.interface';
import { TransportCompensations, TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { isMomentTuple, plainToNew, toRubles } from 'utils';
import { TRangePickerArg } from 'utils/Types';

import {
  IRequestRating,
  TaxiClassEnum,
  TaxiClassTitlesEnum,
  TransportTypeTitlesEnum,
  TripPurpose,
  TripRequest
} from '../Trip.interface';

export class TripRequestModel
  extends RequestBaseModel<TTripRequestStatuses, ApprovalStateStatuses>
  implements TripRequest {
  transportType?: TransportTypeEnum;

  taxiClass?: TaxiClassEnum;

  tariffId?: string;

  requestRating?: IRequestRating;

  author: EmployeeModel;

  passenger: EmployeeModel;

  passengerLimitRemainder: LimitSharing[] = [];

  passengerCount: number;

  desiredDate: number = moment().valueOf();

  expected: RouteModel;

  approvedBy: EmployeeModel;

  purpose: TripPurpose;

  coopTrip: boolean;

  commentForDriver?: string;

  requestOptions?: string[];

  restOfLimit?: number;

  sharedRideId?: string;

  magentaOrderId?: number;

  occupiedPlacesCount?: number;

  personalCar?: PersonalCar;

  compensationType?: TransportCompensations;

  timeZone?: string;

  get isExisting(): boolean {
    return Boolean(this.id);
  }

  get isCancellable(): boolean {
    return !!this.status && TripStatusesCancellable.includes(this.status);
  }

  get isChangeable(): boolean {
    return !!this.status && TripStatusesChangeable.includes(this.status);
  }

  get isFinal(): boolean {
    return !!this.status && TripStatusesFinal.includes(this.status);
  }

  get isFinished(): boolean {
    return this.status === 'TAXI_TRIP_FINISHED';
  }

  get transportTypeString(): string {
    const taxiClass = this.taxiClass && `(${TaxiClassTitlesEnum[this.taxiClass]})`;
    return `${this.transportType && TransportTypeTitlesEnum[this.transportType]} ${taxiClass ?? ''}`;
  }

  get isRated(): boolean {
    return !!this.requestRating;
  }

  get costInRubles(): number | null {
    return this.expected.cost ? Math.round(this.expected.cost / 100) : null;
  }

  inDateRange = (range: TRangePickerArg): boolean => {
    if (isMomentTuple(range)) {
      return moment(this.desiredDate).isBetween(range[0].startOf('day'), range[1].endOf('day'), undefined, '[)');
    }

    return false;
  };

  /**
   * Получение стоимости в рублях.
   */
  getCostInRubles(withFraction = false): number {
    return toRubles(this.expected.cost || 0, withFraction);
  }

  /**
   * Получение остатка по лимиту в рублях.
   */
  getRestOfLimitInRubles(withFraction = false): number {
    // FIXME check restOfLimit after realization on backend
    return toRubles(this.restOfLimit || 0, withFraction);
  }

  // Action должны быть Instance Store.
  @action.bound
  updateState(newState: Partial<this>): void {
    (Object.keys(newState) as (keyof this)[]).forEach(key => {
      this[key] = newState[key] as this[keyof this];
    });
  }

  constructor(trip: TripRequest) {
    super({ ...trip, authorId: trip.author.id });
    this.author = new EmployeeModel(trip.author);
    this.passenger = new EmployeeModel(trip.passenger);
    this.transportType = trip.transportType;
    this.taxiClass = trip.taxiClass;
    this.passengerCount = trip.passengerCount;
    this.tariffId = trip.tariffId;
    this.desiredDate = trip.desiredDate;
    this.coopTrip = trip.coopTrip;
    this.expected = plainToNew<RouteModel>(RouteModel, trip.expected);
    this.approvedBy = new EmployeeModel(trip.approvedBy);
    this.purpose = trip.purpose;
    this.requestRating = trip.requestRating;
    this.requestOptions = trip.requestOptions;
    this.commentForDriver = trip.commentForDriver;
    this.restOfLimit = trip.restOfLimit;
    this.humanReadableId = trip.humanReadableId;
    this.magentaOrderId = trip.magentaOrderId;
    this.sharedRideId = trip.sharedRideId;
    this.requestOptions = trip.requestOptions;
    this.occupiedPlacesCount = trip.occupiedPlacesCount;
    this.personalCar = trip.personalCar;
    this.compensationType = trip.compensationType;
    this.timeZone = trip.timeZone;
  }
}
