import * as t from 'io-ts';
import { DateType, Value } from 'shared/components/DateInput/types';
import { pageAble } from '../Settings/PageSettings';
import { SortSettings } from '../Settings/SortSettings';
import { FeedCargoContent } from './Feed.content';

export const FeedCargo = t.type({
  totalElements: t.number,
  totalPages: t.number,
  number: t.number,
  sort: SortSettings,
  size: t.number,
  content: t.array(FeedCargoContent),
  pageable: pageAble,
  first: t.boolean,
  last: t.boolean,
  numberOfElements: t.number,
  empty: t.boolean,
});
export type FeedCargo = t.TypeOf<typeof FeedCargo>;

export interface FeedCargoSearchQuery {
  id: string;
  status: string;
  addressType: string;
  addressValue: string;
  dateTimeType: DateType;
  date: Value;
  senderName: string;
  recipientName: string;
  page: number;
  pageSize: number;
  sortField: string;
  sortDirection: string;
}

const RequestCargoBodyParamsOTO = t.partial({
  pageSize: t.number,
  page: t.number,
  sortDirection: t.string,
  sortField: t.string,
  id: t.string,
  status: t.array(t.string),
  senderAddress: t.string,
  recipientAddress: t.string,
  senderName: t.string,
  recipientName: t.string,
  creationTimeTo: t.string,
  creationTimeFrom: t.string,
  desiredTimeTo: t.string,
  desiredTimeFrom: t.string,
  approvalTimeTo: t.string,
  approvalTimeFrom: t.string,
  transferTimeTo: t.string,
  transferTimeFrom: t.string,
  shipmentTimeTo: t.string,
  shipmentTimeFrom: t.string,
});

export type RequestCargoBodyParamsOTO = t.TypeOf<typeof RequestCargoBodyParamsOTO>;
