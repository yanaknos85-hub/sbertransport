import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { toRubles } from 'utils';

import { plainToNew } from 'utils/Misc';

import { TransportTypeEnum } from '../../stores/TransportTypes/TransportTypes.interface';
import { TaxiClassEnum, TaxiClassTitlesEnum, TransportTypeTitlesEnum } from '../../stores/Trip/Trip.interface';
import { ApprovalTypeEnum, TApprovement } from './Approval.interface';

export enum ApprovalsStatusEnum {
  NEW = 'NEW',
  EDIT = 'EDITED',
  APPROVED = 'APPROVED',
  DECLINED = 'DECLINED',
  CANCELLED = 'CANCELLED',
}

export class ApprovalModel {
  approvalType: ApprovalTypeEnum | undefined;

  cost: number;

  desiredDate: string;

  requestHumanReadableId: string;

  id: string;

  passengerId?: string;

  restOfLimit?: number;

  taxiClass?: TaxiClassEnum;

  transportType: TransportTypeEnum;

  status: ApprovalsStatusEnum;

  expectedTime: number;

  expectedDistance: number;

  purpose: string | undefined;

  purposeId: string | undefined;

  purposeLabel: string | undefined;

  passengerCount: number;

  waypoints: WaypointModel[];

  requestId: string;

  constructor(approval: TApprovement) {
    this.approvalType = approval.type;
    this.cost = approval.cost;
    this.passengerId = approval?.passengerId;
    this.requestHumanReadableId = approval.requestHumanReadableId;
    this.taxiClass = approval.taxiClass;
    this.transportType = approval.transportType;
    this.expectedTime = approval.expectedTime;
    this.expectedDistance = approval.expectedDistance;
    this.purpose = approval.purpose;
    this.purposeId = approval.purposeId;
    this.purposeLabel = approval.purposeLabel;
    this.status = approval.status;
    this.id = approval.id;
    this.restOfLimit = approval.restOfLimit;
    this.waypoints = plainToNew(WaypointModel, approval.waypoints);
    this.passengerCount = approval.passengerCount;
    this.requestId = approval.requestId;
    this.desiredDate = approval.desiredDate;
  }

  get transportTypeString(): string {
    const taxiClass = this.taxiClass && `(${TaxiClassTitlesEnum[this.taxiClass]})`;
    return `${this.transportType && TransportTypeTitlesEnum[this.transportType]} ${taxiClass ?? ''}`;
  }

  getTimeString(): string {
    const time = this.expectedTime / 60000;

    if (time < 60) {
      return `${Math.round(time)} мин`;
    }

    return `${Math.trunc(time / 60)} ч ${Math.round(time % 60)} мин`;
  }

  getDistanceString(): string {
    return `${this.expectedDistance.toFixed(2)} км`;
  }

  getStatusString(): string {
    const onApprovalText = 'На согласовании';
    const routeApprovalText = 'Утверждение маршрута';
    switch (true) {
      case this.status === ApprovalsStatusEnum.NEW && this.approvalType === ApprovalTypeEnum.TRIP:
        return routeApprovalText;
      case this.status === ApprovalsStatusEnum.NEW:
        return onApprovalText;
      case this.status === ApprovalsStatusEnum.EDIT && this.approvalType === ApprovalTypeEnum.TRIP:
        return routeApprovalText;
      case this.status === ApprovalsStatusEnum.EDIT:
        return `${onApprovalText} (изменено)`;
      case this.status === ApprovalsStatusEnum.APPROVED:
        return 'Согласовано';
      case this.status === ApprovalsStatusEnum.DECLINED:
        return 'Отклонено';
      case this.status === ApprovalsStatusEnum.CANCELLED:
        return 'Отменено';
      default:
        return 'Статус не определён';
    }
  }

  /**
   * Получение стоимости в рублях.
   */
  getCostInRubles(withFraction = false): number {
    return toRubles(this.cost, withFraction);
  }

  /**
   * Получение остатка по лимиту в рублях.
   */
  getRestOfLimitInRubles(withFraction = false): number {
    return toRubles(this.restOfLimit || 0, withFraction);
  }
}
