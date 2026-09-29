import { Employee } from '@sber-sbertransport/mf-core';
import { RequestBaseModel } from 'shared/models/RequestBase.model';
import { ApprovalStateStatuses } from 'shared/models/types';

import { CargoRequestStatusesType } from 'constants/CargoRequestStatuses.constants';

import { CargoRegularRequestNewType, CargoRequestType, PeriodType } from '../types';

export class CargoRegularRequestModel
  extends RequestBaseModel<CargoRequestStatusesType, ApprovalStateStatuses>
  implements CargoRegularRequestNewType {
  humanReadableId: string;

  id: string;

  period: PeriodType;

  template: Partial<CargoRequestType>;

  active: boolean;

  creationTime: string | number;

  status: CargoRequestStatusesType;

  approvalState: ApprovalStateStatuses;

  approvedBy: Employee[];

  allCost: number;

  source: string;

  constructor(cargo: CargoRegularRequestNewType) {
    super({ ...cargo });
    this.humanReadableId = cargo.humanReadableId;
    this.id = cargo.id;
    this.period = cargo.period;
    this.template = cargo.template;
    this.active = cargo.active;
    this.creationTime = cargo.creationTime;
    this.status = cargo.status;
    this.approvalState = cargo.approvalState;
    this.approvedBy = cargo.approvedBy;
    this.allCost = cargo.allCost;
    this.source = cargo.source;
  }
}
