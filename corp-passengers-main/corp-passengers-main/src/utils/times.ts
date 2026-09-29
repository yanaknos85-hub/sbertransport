import moment from 'moment-timezone';

// eslint-disable-next-line @stylistic/max-len
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

export const formattedTimezoneToUTC = () => {
  const currentTime = moment();
  const utcOffset = currentTime.utcOffset();

  const hours = Math.abs(Math.floor(utcOffset / 60));
  const sign = utcOffset >= 0 ? '+' : '-';

  return `${sign}${hours.toString().padStart(2, '0')}`;
};
