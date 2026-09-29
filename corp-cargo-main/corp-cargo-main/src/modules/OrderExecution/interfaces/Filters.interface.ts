import type { Moment } from 'moment';
import { OrderField } from '../constants/Cargo/Cargo';

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
  [key: string]: unknown;
  [OrderField.authorDepartment]?: string,
  [OrderField.creationTime]: IFilterDate | undefined,
  [OrderField.desiredTime]: IFilterDate | undefined,
  [OrderField.recipientAddress]?: string;
  [OrderField.recipientName]?: string;
  [OrderField.requestType]?: string;
  [OrderField.senderAddress]?: string;
  [OrderField.senderName]?: string;
  [OrderField.status]?: string | string[];
  [OrderField.id]?: string;
  humanReadableId?: string;
  sortSetting?: SortSetting<string>;
  pageSetting?: PageSetting;
}
