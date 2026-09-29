import moment from 'moment';
import { TWaypoint } from 'shared/models/geo/types';

import { DATE_FORMAT } from 'constants/constants.app';

export const buildAddressString = (waypoint: TWaypoint): string => (
  `${waypoint?.region ? `${waypoint.region}, ` : ''} `
  + `${waypoint?.district ? `${waypoint.district}, ` : ''} `
  + `${waypoint?.city ? `${waypoint.city.replace(/\s/, ' ')}, ` : ''} `
  + `${waypoint?.livingArea ? `${waypoint.livingArea}, ` : ''} `
  + `${waypoint?.settlement ? `${waypoint.settlement}, ` : ''} `
  + `${waypoint?.place ? `${waypoint.place}, ` : ''} `
  + `${waypoint?.street ? `${waypoint.street}, ` : ''}`
  + `${waypoint?.house ? `${waypoint.house}, ` : ''}`
  + `${waypoint?.building ? `${waypoint.building}, ` : ''}`
  + `${waypoint?.structure ? `${waypoint.structure}` : ''}`
).replace(/,\s*$/, '').trim();

export const buildDate = (ms: number): string => moment(ms).utc().format(DATE_FORMAT.DATE_WITH_TIME_DOTS);
export const buildDateRegular = (value: number) => moment(value).utc().format(DATE_FORMAT.BASE_REVERTED_DOTS);

/* на данный момент временно не используется */
export const desiredFormatDate = (desiredDate: Date) => (
  moment(desiredDate)
    .utc()
    .set({
      hours: 12,
      minutes: 0,
      seconds: 0,
    })
    .toDate()
    .valueOf()
);

export const getUnixDateWithOffset = (desiredDate: moment.Moment): number => {
  const offset = moment(desiredDate).utcOffset() * 60 * 1000;
  const unixDesiredDate = moment(desiredDate).valueOf() + offset;
  return unixDesiredDate;
};
