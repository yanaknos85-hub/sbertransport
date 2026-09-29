import { Employee, EmployeeModel, Segment } from '@sber-sbertransport/mf-core';
import moment from 'moment';
import { RouteModel } from 'shared/models/geo/Route.model';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { RequestBaseModel } from 'shared/models/RequestBase.model';
import { ApprovalStateStatuses } from 'shared/models/types';
import { RequestType } from 'shared/models/types';
import { plainToNew } from 'utils';

import { Package } from 'stores/Cargo/Cargo.interface';
import { CargoTypeNameEnum } from 'stores/CargoType/CargoType.interface';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { CalculatedTariff, CargoListItem, TotalSizes } from 'types/Cargo';
import { CargoRequestStatusesType } from 'constants/CargoRequestStatuses.constants';

import { CargoRequestType, CarInfoType, PeriodType } from '../types';

export class CargoRequestModel
  extends RequestBaseModel<CargoRequestStatusesType, ApprovalStateStatuses>
  implements CargoRequestType {
  transportType?: TransportTypeEnum;

  transportTypeRus?: string;

  author: EmployeeModel | null;

  sender: EmployeeModel | null; //  fixme in future it moved somewhere

  recipient: EmployeeModel | null; // fixme in future it moved somewhere

  recipient2?: EmployeeModel; // fixme in future it moved somewhere

  recipient3?: EmployeeModel; // fixme in future it moved somewhere

  desiredDate: number = moment().valueOf();

  deliveryTimeDate: number = moment().valueOf();

  nextDelivery?: number;

  expected: RouteModel;

  segments: Segment[] = [];

  approvedBy: Employee[];

  express: boolean;

  restOfLimit?: number;

  comment?: string;

  commentEng?: string;

  senderOrganization?: string; // fixme in future it moved somewhere

  recipientOrganization?: string; //  fixme in future it moved somewhere

  listCargo: CargoListItem[];

  cargoList?: CargoTypeNameEnum[];

  requestType?: RequestType;

  packages?: Package[];

  loaders?: number;

  cargoDetails?: CargoListItem[];

  totalSizes: TotalSizes;

  sourceLoaders?: boolean;

  destinationLoaders?: boolean;

  tariffId?: string;

  occupiedPlacesCount?: number;

  creationTime: string | number;

  period?: PeriodType;

  active?: boolean;

  approvalState?: ApprovalStateStatuses;

  allCost?: number;

  volume?: number;

  weight?: number;

  totalCost?: number;

  resolution?: string;

  calculatedTariff?: CalculatedTariff;

  sourceLoadersCount?: number;

  destinationLoadersCount?: number;

  internalNote?: string;

  waypoints: WaypointModel[] = [];

  carInfo?: CarInfoType;

  qrs?: string[];

  constructor(cargo: CargoRequestType) {
    super({ ...cargo, authorId: cargo.author?.id });
    this.author = cargo.author ? new EmployeeModel(cargo.author) : null;
    this.sender = cargo.sender ? new EmployeeModel(cargo.sender) : null;
    this.recipient = cargo.recipient ? new EmployeeModel(cargo.recipient) : null;
    this.recipient2 = new EmployeeModel(cargo.recipient2);
    this.recipient3 = new EmployeeModel(cargo.recipient3);
    this.transportType = cargo.transportType;
    this.transportTypeRus = cargo.transportTypeRus;
    this.requestType = cargo.requestType;
    this.packages = cargo.packages;
    this.loaders = cargo.loaders;
    this.desiredDate = cargo.desiredDate;
    this.deliveryTimeDate = cargo.deliveryTimeDate;
    this.express = cargo.express;
    this.expected = plainToNew<RouteModel>(RouteModel, cargo.expected);
    this.segments = cargo.segments || [];
    this.approvedBy = cargo.approvedBy;
    this.restOfLimit = cargo.restOfLimit;
    this.humanReadableId = cargo.humanReadableId;
    this.comment = cargo.comment;
    this.commentEng = cargo.commentEng;
    this.senderOrganization = cargo.senderOrganization;
    this.recipientOrganization = cargo.recipientOrganization;
    this.listCargo = cargo.listCargo;
    this.totalSizes = cargo.totalSizes;
    this.sourceLoaders = cargo.sourceLoaders;
    this.destinationLoaders = cargo.destinationLoaders;
    this.tariffId = cargo.tariffId;

    this.cargoDetails = cargo.cargoDetails;
    this.occupiedPlacesCount = cargo.occupiedPlacesCount;
    this.creationTime = cargo.creationTime;
    this.period = cargo.period;
    this.active = cargo.active;
    this.approvalState = cargo.approvalState;
    this.nextDelivery = cargo.nextDelivery;
    this.allCost = cargo.allCost;
    this.volume = cargo.volume;
    this.weight = cargo.weight;
    this.totalCost = cargo.totalCost;
    this.resolution = cargo.resolution;
    this.calculatedTariff = cargo.calculatedTariff;
    this.sourceLoadersCount = cargo.sourceLoadersCount;
    this.destinationLoadersCount = cargo.destinationLoadersCount;
    this.internalNote = cargo.internalNote;
    this.requestType = cargo.requestType;
    this.waypoints = cargo.waypoints?.map(waypoint => new WaypointModel(waypoint)) || [];
    this.carInfo = cargo.carInfo;
    this.qrs = cargo.qrs;
  }
}
