
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import { RequestBaseModel } from 'shared/models/RequestBase.model';

import {
  LimitRequestApprovalStateEnum,
  LimitRequestStatusEnum,
  LimitRequestStatusesCancellable,
  LimitRequestStatusesFinal,
  TLimitRequestNew
} from '../LimitsRequest.interface';

export class LimitRequestModel
  extends RequestBaseModel<LimitRequestStatusEnum, LimitRequestApprovalStateEnum>
  implements TLimitRequestNew {
  transportType?: TransportTypeEnum;

  description?: string;

  month?: number;

  sum: number;

  constructor(request: TLimitRequestNew & RequestBaseModel) {
    super(request);
    this.sum = request.sum;
    this.month = request.month;
    this.transportType = request.transportType;
  }

  get isCancellable(): boolean {
    return !!this.status && LimitRequestStatusesCancellable.includes(this.status);
  }

  get isFinal(): boolean {
    return !!this.status && LimitRequestStatusesFinal.includes(this.status);
  }
}
