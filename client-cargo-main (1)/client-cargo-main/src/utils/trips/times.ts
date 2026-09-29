import moment from 'moment-timezone';
import { emptySign } from 'shared/constants/constants';

import { DATE_FORMAT } from 'constants/constants.app';

export const zoneTime = (time: string | number | undefined, zone?: string | undefined): string => time && +time > 0
  ? moment(time)
    .utcOffset(zone ?? 'GMT+03')
    .format(DATE_FORMAT.DATE_WITH_TIME)
  : emptySign;
