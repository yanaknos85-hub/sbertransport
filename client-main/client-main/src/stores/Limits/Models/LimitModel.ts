import { UUID } from 'utils/io-ts';

import {
  Department,
  LIMIT_SERVICE_TYPE,
  LIMIT_SHARING_TYPE,
  LIMIT_STATUS,
  LIMIT_TYPE,
  Limit
} from '../Limit.interface';

export class LimitModel implements Limit {
  department?: Department;

  parentLimitId?: UUID;

  id: UUID;

  limitOwner: UUID;

  year: number;

  sum: number;

  reserve: number;

  limitType: LIMIT_TYPE;

  limitStatus: LIMIT_STATUS;

  limitSharingType: LIMIT_SHARING_TYPE;

  limitServiceType: LIMIT_SERVICE_TYPE;

  finalSharing: boolean;

  useThisLimit: boolean;

  humanReadableId: string;

  responsibles: UUID[];

  constructor(limit: Limit) {
    this.id = limit.id;
    this.humanReadableId = limit.humanReadableId;
    this.limitOwner = limit.limitOwner;
    this.year = limit.year;
    this.sum = limit.sum;
    this.reserve = limit.reserve;
    this.limitType = limit.limitType;
    this.limitStatus = limit.limitStatus;
    this.limitSharingType = limit.limitSharingType;
    this.limitServiceType = limit.limitServiceType;
    this.finalSharing = limit.finalSharing;
    this.useThisLimit = limit.useThisLimit;
    this.department = limit.department;
    this.parentLimitId = limit.parentLimitId;
    this.responsibles = limit.responsibles;
  }

  get availablePersentage(): number {
    return Number(((this.sum * 100) / this.sum).toFixed(1));
  }
}
