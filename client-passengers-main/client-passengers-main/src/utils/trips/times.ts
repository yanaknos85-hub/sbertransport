import moment from 'moment-timezone';

export const zoneTime = (time: string | number | undefined, formatDate: string, zone?: string | undefined): string => time && +time > 0
  ? moment(time)
    .utcOffset(zone ?? 'GMT+03')
    .format(formatDate)
  : '-';

export const zoneTimeISODate = (time: string | undefined, formatDate: string, zone?: string | undefined) => time
  ? moment(`${time}Z`)
    .utcOffset(zone ?? 'GMT+03')
    .format(formatDate)
  : '-';
