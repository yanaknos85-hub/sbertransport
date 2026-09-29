import * as R from 'ramda';

import { TransportTypeEnum } from '../stores/TransportTypes/TransportTypes.interface';

export const filterByTransportType = <T extends { transportType: TransportTypeEnum | string }>(
  fullData: T[],
  transportType: TransportTypeEnum
): T[] => R.filter(R.whereEq({ transportType }))(fullData);
