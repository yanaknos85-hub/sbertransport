/* eslint-disable @stylistic/implicit-arrow-linebreak */
/* eslint-disable @typescript-eslint/no-explicit-any */
import moment from 'moment';

import { LIMIT_REQUEST_STATUS } from 'stores/Limits/Limit.interface';

import { UUID } from 'utils/io-ts';
import { TRangePickerArg, isMomentTuple } from 'utils/Types';

import { ISpentActionsType } from '../../useDetailedLimitsMapper';

const getCreationTime = (date: ISpentActionsType): number => moment(date.creationTime).valueOf();

export const getActiveRequests = (limits: ISpentActionsType[]): ISpentActionsType[] => limits.filter(x => x.status === LIMIT_REQUEST_STATUS.INIT).sort((a, b) => getCreationTime(b) - getCreationTime(a));

export const getUnActiveRequests = (limits: ISpentActionsType[], empId: string | UUID): ISpentActionsType[] => limits
  .filter(x => x.author.id === empId && x.status !== LIMIT_REQUEST_STATUS.INIT)
  .sort((a, b) => getCreationTime(b) - getCreationTime(a));

export const getDataWithFilters = (
  dataByTransportType: ISpentActionsType[],
  range: TRangePickerArg
): ISpentActionsType[] =>
  // eslint-disable-next-line array-callback-return, consistent-return
  dataByTransportType.filter(x => {
    // FIXME array-callback-return, consistent-return
    if (isMomentTuple(range)) {
      return moment(x.creationTime).isBetween(range[0].startOf('day'), range[1].endOf('day'), undefined, '[)');
    }
  });

export const isRealArray = (array: any[]): boolean => {
  if (Array(array)) {
    return array.every(x => x);
  }
  return true;
};
