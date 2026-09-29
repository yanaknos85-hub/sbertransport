import { ColumnType } from 'antd/lib/table/interface';
import { Value as DateFormValue } from 'shared/components/DateInput/types';
import { NumberRange, SearchRequest } from 'stores/CargoRegistry/CargoRegistry.interface';
import { DateRangeISO } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { RangeNumber } from 'api/register-search';
import { VisibleFields } from './constants';

export type Row = {
  id: string;
} & { [key in VisibleFields]?: string };

export type Column = Omit<ColumnType<Row>, 'dataIndex'> & {
  dataIndex: VisibleFields;
  sortProperty?: string;
};

export interface FilterValues {
  requestHumanId: string;
  desiredDate: DateFormValue;
  creationDate: DateFormValue;
  changeDate: DateFormValue;
  expectedCost: NumberRange;
  contractorSet: string[];
  authorFIO: string;
  deadlineDate: boolean;
  requestStatusSet: string[];
  requestTypeSet: string[];
  cargoTransportType?: string[];
}

export type TransformedFilterValues = {
  requestHumanId?: string;
  desiredDate?: DateRangeISO;
  creationDate?: DateRangeISO;
  changeDate?: DateRangeISO;
  expectedCost?: RangeNumber;
  contractorSet?: string[];
  authorFIO?: string;
  deadlineDate?: boolean | string;
  requestStatusSet?: string[];
  requestTypeSet?: string[];
  cargoTransportType?: string[];
};

export type ColumnVisibilitySettings = Partial<Record<VisibleFields, boolean>>;

export type RequestBodyCargoParams = { cargoUIVisibilityDTO?: ColumnVisibilitySettings } & SearchRequest;
