import { observable } from 'mobx';
import moment from 'moment';
import { isMomentTuple, TRangePickerArg } from 'utils';

import { IEntityBase, IEntityStatused } from './Entity.interface';
import { IRequestBase } from './Request.interface';

export class RequestBaseModel<S extends string = any, A extends string = any>
implements IRequestBase, IEntityBase, IEntityStatused {
  authorId?: string;

  approvalState?: A;

  @observable
    status?: S;

  id: string;

  humanReadableId: string;

  creationTime: string | number;

  constructor(request: IRequestBase) {
    this.id = request.id;
    this.humanReadableId = request.humanReadableId;
    this.status = request.status as S;
    this.creationTime = request.creationTime;
    this.approvalState = request.approvalState as A;
    this.authorId = request.authorId;
  }

  get isExisting(): boolean {
    return !!this.id;
  }

  isMatched(statuses?: string[]): boolean {
    return !!this.status && !!statuses?.includes(this.status);
  }

  inDateRange = (range: TRangePickerArg): boolean => {
    if (isMomentTuple(range)) {
      return moment(this.creationTime).isBetween(range[0].startOf('day'), range[1].endOf('day'), undefined, '[)');
    }

    return false;
  };
}
