import { action } from 'mobx';

import { RequestListItem, ErrorStructure } from '../SRMMassMultiple.interface';

export class RequestModel implements RequestListItem {
  personnelNumber: ErrorStructure;
  fullName: ErrorStructure;
  addressFrom: ErrorStructure;
  addressTo: ErrorStructure;
  orderTime: ErrorStructure;
  orderDate: ErrorStructure;
  transportClass: ErrorStructure;
  transportType: ErrorStructure;
  tripType: ErrorStructure;
  addressFromLatitude: number;
  addressFromLongitude: number;
  addressToLatitude: number;
  addressToLongitude: number;

  // Action должны быть Instance Store.
  @action.bound
  updateState(newState: Partial<this>): void {
    (Object.keys(newState) as (keyof this)[]).forEach(key => {
      this[key] = newState[key] as this[keyof this];
    });
  }

  constructor(list: RequestListItem) {
    this.personnelNumber = list.personnelNumber;

    this.fullName = list.fullName;
    this.addressFrom = list.addressFrom;
    this.addressTo = list.addressTo;

    this.orderTime = list.orderTime;
    this.orderDate = list.orderDate;

    this.transportClass = list.transportClass;
    this.transportType = list.transportType;
    this.tripType = list.tripType;
    this.addressFromLatitude = list.addressFromLatitude;
    this.addressFromLongitude = list.addressFromLongitude;
    this.addressToLatitude = list.addressToLatitude;
    this.addressToLongitude = list.addressToLongitude;
  }
}
