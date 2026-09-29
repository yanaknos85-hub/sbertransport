import moment, { Moment } from 'moment/moment';

export const localTimeStringToUtcString = (local: string): string => moment(local).utc().format();
export const utcTimeStringToLocalMoment = (utc?: string): Moment => moment.utc(utc).local();
