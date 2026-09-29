import { ILimitDetailed, ILimitValueDetailed } from '../Limit.interface';

export class LimitModelDetailed implements ILimitDetailed {
  id: string;

  value: ILimitValueDetailed;

  fromDate: string;

  toDate: string;

  transportTypeId: string;

  constructor(limit: ILimitDetailed) {
    this.id = limit.id;
    this.value = limit.value;
    this.fromDate = limit.fromDate;
    this.toDate = limit.toDate;
    this.transportTypeId = limit.transportTypeId;
  }
}
