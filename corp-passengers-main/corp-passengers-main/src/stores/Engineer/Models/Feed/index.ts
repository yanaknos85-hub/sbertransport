import * as t from 'io-ts';
import { DateType, Value } from 'shared/components/DateInput/types';
import { pageAble } from '../Settings/PageSettings';
import { SortSettings } from '../Settings/SortSettings';
import { FeedContent } from './Feed.content';

export const Feed = t.type({
  totalElements: t.number,
  totalPages: t.number,
  number: t.number,
  sort: SortSettings,
  size: t.number,
  content: t.array(FeedContent),
  pageable: pageAble,
  first: t.boolean,
  last: t.boolean,
  numberOfElements: t.number,
  empty: t.boolean,
});
export type Feed = t.TypeOf<typeof Feed>;

export interface FeedSearchQuery {
  id: string;
  status: string;
  transportType: string;
  contractor: string;
  addressType: string;
  addressValue: string;
  passengerDepartment: string;
  passengerPhone: string;
  passengerPosition: string;
  dateTimeType: DateType;
  date: Value;
  deadline: Value;
  address: string;
  passengerName: string;
  transportClass: string;
  page: number;
  pageSize: number;
  sortField: string;
  sortDirection: string;
}

const RequestBodyParamsOTO = t.partial({
  pageSize: t.number,
  page: t.number,
  sortField: t.string,
  sortDirection: t.string,
  id: t.string,
  status: t.array(t.string),
  transportType: t.string,
  contractor: t.string,
  departure: t.string,
  waypoint: t.string,
  destination: t.string,
  passengerName: t.string,
  transportClass: t.string,
  creationTimeFrom: t.string,
  startTimeFrom: t.string,
  finishTimeFrom: t.string,
  desiredTimeFrom: t.string,
  creationTimeTo: t.string,
  startTimeTo: t.string,
  finishTimeTo: t.string,
  desiredTimeTo: t.string,
  deadlineFrom: t.string,
  deadlineTo: t.string,
  deadlineState: t.string,
  passengerPhone: t.string,
  passengerPosition: t.string,
  passengerDepartment: t.string,
});

export type RequestBodyParamsOTO = t.TypeOf<typeof RequestBodyParamsOTO>;
