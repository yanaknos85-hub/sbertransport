import type { Moment } from 'moment';

export interface PageSetting {
  page: number;
  size: number;
}

export interface SortSetting<T> {
  property: T;
  directionAsc: boolean;
}

export interface IFilterDate {
  value: [Moment, Moment];
}

export interface IFilter {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  [key: string]: any;
  humanReadableId?: string;
  dispatcherRequest?: boolean;
  sortSetting?: SortSetting<string>;
  pageSetting?: PageSetting;
}
