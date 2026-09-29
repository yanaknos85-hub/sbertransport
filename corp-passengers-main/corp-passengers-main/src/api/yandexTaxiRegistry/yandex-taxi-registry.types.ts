import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { Waypoint } from 'stores/Geo/Geo.interface';
import { YandexTaxiTariff } from './yandex-taxi-registry.constants';

export const RangeNumber = t.strict({
  start: t.number,
  end: t.number,
});
export type RangeNumber = t.TypeOf<typeof RangeNumber>;

export const DateRangeISO = t.strict({
  start: t.string,
  end: t.string,
});
export type DateRangeISO = t.TypeOf<typeof DateRangeISO>;

export const PaymentStateResponse = t.array(
  t.type({
    requestId: t.string,
    status: t.string,
  })
);

export type PaymentStateResponse = t.TypeOf<typeof PaymentStateResponse>;

export const SortSetting = t.partial({
  sortField: t.string,
  sortDirection: t.boolean,
  directionAsc: t.boolean,
  property: t.string,
});
export type SortSetting = t.TypeOf<typeof SortSetting>;

export const PageSetting = t.type({
  page: t.number,
  size: t.number,
});
export type PageSetting = t.TypeOf<typeof PageSetting>;

const SortDirection = t.keyof({
  DESC: null,
  ASC: null,
});
export type SortDirection = t.TypeOf<typeof SortDirection>;

export const YandexTaxiReportItem = t.partial({
  id: tt.uuid,
  tariff: ioTypeFromEnum('tariff', YandexTaxiTariff), // enum - тарифы. value или русское нименование
  passengerId: tt.uuid,
  passenger: t.partial({
    id: tt.uuid,
    firstName: t.string,
    lastName: t.string,
    patronymic: t.string,
  }),
  approver: t.partial({
    id: tt.uuid,
    firstName: t.string,
    lastName: t.string,
    patronymic: t.string,
  }),
  humanReadableId: t.string,
  tripDate: t.string, // iso-8601
  waypoints: t.array(Waypoint),
  plannedCost: t.number, // рубли
  plannedDuration: t.string, // iso period
  factCost: tt.nullable(t.number), // рубли
  purposeId: tt.uuid,
  status: t.string,
  comment: tt.nullable(t.string),
  reason: tt.nullable(t.string),
  link: tt.nullable(t.string), // ссылка на чек
  receipt: tt.nullable(t.string), // имя файла чека (=humanreadableid)
  costCenter: tt.nullable(t.string),
});

export type YandexTaxiReportItem = t.TypeOf<typeof YandexTaxiReportItem>;

export const YandexTaxiReportResponse = t.type({
  content: t.array(YandexTaxiReportItem),
  sort: t.type({
    field: t.string,
    direction: SortDirection,
  }),
  page: t.type({
    number: t.number,
    size: t.number,
    first: t.boolean,
    last: t.boolean,
    total: t.number,
    count: t.number,
  }),
});
export type YandexTaxiReportResponse = t.TypeOf<typeof YandexTaxiReportResponse>;

export const OrderPaymentFormationStartRangeFilter = t.type({
  startDate: t.string,
  endDate: t.string,
});

export const DepartmentsFilter = t.type({
  1: t.array(tt.uuid),
  2: t.array(tt.uuid),
  3: t.array(tt.uuid),
  4: t.array(tt.uuid),
  5: t.array(tt.uuid),
  6: t.array(tt.uuid),
});

export const YandexTaxiReportFilter = t.partial({
  organizationId: tt.uuid,
  humanReadableId: t.string,
  balanceUnits: t.array(t.number),
  costCenter: t.string,
  status: t.array(t.string),
  passenger: t.array(tt.uuid),
  approver: t.array(tt.uuid),
  startFrom: t.string,
  startTo: t.string,
  passengerName: t.string,
  approverName: t.string,
  orderPaymentFormationStartRange: OrderPaymentFormationStartRangeFilter,
  departments: DepartmentsFilter,
});
export type YandexTaxiReportFilter = t.TypeOf<typeof YandexTaxiReportFilter>;

export const YandexTaxiReportSearchQuery = t.partial({
  filter: YandexTaxiReportFilter,
  page: t.number,
  size: t.number,
  sort: t.string,
  direction: t.string,
});
export type YandexTaxiReportSearchQuery = t.TypeOf<typeof YandexTaxiReportSearchQuery>;

export interface YandexTaxiUpdateStatusesParams {
  status: string;
  requestId: string;
}
