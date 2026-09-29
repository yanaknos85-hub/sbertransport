import { ColumnProps } from 'antd/lib/table';
import { FeedContent } from 'stores/Engineer/Models/Feed/Feed.content';

export interface TableRecord {
  humanReadableId: string;
  status: string;
  transportType: string;
  passenger: string;
  positionName: string;
  phone: string;
  commentForDriver: string;
  departureAddress: string;
  waypointAddress: string;
  destinationAddress: string;
  creationTime: string;
  desiredDate: string;
  deadline: string;
  expectedDistance: string;
  expectedCost: string;
  waitTime: string;
  passengerCount: number | string;
  tariffId: number | string;
  taxiClass: string;
}

export type FilterValues = Omit<TableRecord, 'pageSetting' | 'sortSetting'>;

export enum SortFields {
}

export type OrderExecutionColumnProps = ColumnProps<FeedContent> &
  ({ sorting: true; sortProperty: SortFields } | { sorting?: false; sortProperty?: never }) & { checked?: boolean };
