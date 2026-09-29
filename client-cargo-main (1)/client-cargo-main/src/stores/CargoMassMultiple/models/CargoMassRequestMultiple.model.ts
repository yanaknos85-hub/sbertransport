import { EmployeeModel } from '@sber-sbertransport/mf-core';
import { action } from 'mobx';
import { RouteModel } from 'shared/models/geo/Route.model';
import { RequestBaseModel } from 'shared/models/RequestBase.model';
import { plainToNew } from 'utils';

import { TariffCost } from 'stores/CargoTariff/CargoTariff.interface';
import { CargoListItem } from 'types/Cargo';

import {
  RequestListItem, RequestStatus, TotalSizes, User
} from '../CargoMassMultiple.interface';

export class RequestModel extends RequestBaseModel implements RequestListItem {
  status: RequestStatus;

  id: string;

  author: EmployeeModel;

  externalId?: string;

  express: boolean;

  senderOrganization: string;

  recipientOrganization: string;

  sourceLoaders: boolean;

  destinationLoaders: boolean;

  desiredDate: number;

  desiredDateTime: number;

  deliveryTimeDate: number;

  sender: User;

  recipient: User;

  listCargo: CargoListItem[];

  tariffs: TariffCost[];

  totalSizes: TotalSizes;

  expected: RouteModel;

  description: string;

  comment?: string;

  sourceLoadersCount: number;

  destinationLoadersCount: number;

  requestNumber?: number;

  approver?: string;

  // Action должны быть Instance Store.
  @action.bound
  updateState(newState: Partial<this>): void {
    (Object.keys(newState) as (keyof this)[]).forEach(key => {
      this[key] = newState[key] as this[keyof this];
    });
  }

  constructor(cargo: RequestListItem) {
    super({ ...cargo, authorId: cargo.author.id });
    this.author = new EmployeeModel(cargo.author);

    this.status = cargo.status;
    this.id = cargo.id;
    this.externalId = cargo.externalId;
    this.express = cargo.express;

    this.sourceLoaders = cargo.sourceLoaders;
    this.destinationLoaders = cargo.destinationLoaders;

    this.desiredDate = cargo.desiredDate;
    this.desiredDateTime = cargo.desiredDateTime;
    this.deliveryTimeDate = cargo.deliveryTimeDate;

    this.senderOrganization = cargo.senderOrganization;
    this.recipientOrganization = cargo.recipientOrganization;

    this.sender = cargo.sender;
    this.recipient = cargo.recipient;

    this.listCargo = cargo.listCargo;
    this.tariffs = cargo.tariffs;
    this.totalSizes = cargo.totalSizes;
    this.expected = plainToNew<RouteModel>(RouteModel, cargo.expected);

    this.description = cargo.description;
    this.comment = cargo.comment;
    this.sourceLoadersCount = cargo.sourceLoadersCount;
    this.destinationLoadersCount = cargo.destinationLoadersCount;
    this.requestNumber = cargo.requestNumber;
    this.approver = cargo.approver;
  }
}
