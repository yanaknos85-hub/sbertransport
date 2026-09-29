import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { YandexTaxiTariff } from './yandex-taxi.constants';

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

export const Waypoint = t.intersection([
  t.type({
    latitude: t.number,
    longitude: t.number,
  }),
  t.partial({
    country: t.string,
    region: t.string,
    city: t.string,
    street: t.string,
    house: t.string,
    building: t.string,
    structure: t.string,
    checkinManual: t.boolean,
    checkinAutomatic: t.boolean,
    existInVspGosbTbRegistry: t.boolean,
    waitTime: t.number,
  }),
]);

export type Waypoint = t.TypeOf<typeof Waypoint>;

export const FraudCommentResponseSchema = t.intersection([
  t.type({
    text: t.string,
  }),
  t.partial({
    id: t.string,
    humanReadableId: t.string,
  }),
]);

export const YandexTaxiRequest = t.partial({
  id: tt.uuid,
  tariff: ioTypeFromEnum('tariff', YandexTaxiTariff),
  passengerId: tt.uuid, // приходит если в запросе параметр format = LIST
  passenger: t.partial({ // приходит если в запросе параметр format = FULL или REGISTRY
    id: tt.uuid,
    firstName: t.string,
    lastName: t.string,
    patronymic: t.string,
  }),
  approver: t.partial({ // приходит если в запросе параметр format = REGISTRY
    id: tt.uuid,
    firstName: t.string,
    lastName: t.string,
    patronymic: t.string,
  }),
  fraudComment: t.array(FraudCommentResponseSchema),
  humanReadableId: t.string,
  tripDate: t.string, // iso-8601
  waypoints: t.array(Waypoint),
  plannedCost: t.number, // рубли!
  plannedDuration: t.string, // iso period
  factCost: tt.nullable(t.number), // рубли!
  purposeId: tt.uuid,
  status: t.string,
  comment: tt.nullable(t.string),
  reason: tt.nullable(t.string),
  link: tt.nullable(t.string), // ссылка на чек. всегда null??
  receipt: tt.nullable(t.string), // имя файла чека (=humanreadableid)
});

export type YandexTaxiRequest = t.TypeOf<typeof YandexTaxiRequest>;

export const YandexTaxiRequestResponse = t.type({
  content: t.array(YandexTaxiRequest),
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
export type YandexTaxiRequestResponse = t.TypeOf<typeof YandexTaxiRequestResponse>;

export const YandexTaxiRequestSearchQuery = t.partial({
  humanReadableId: t.string,
  approvalStatus: t.boolean,
  requestStatusSet: t.array(t.string),
  tariffSet: t.array(t.string),
  factDistance: RangeNumber,
  creationDate: DateRangeISO,
  desiredDateRange: DateRangeISO,
  passengerFullName: t.string,
  approverFullName: t.string,
  sortSetting: SortSetting,
  pageSetting: PageSetting,
});
export type YandexTaxiRequestSearchQuery = t.TypeOf<typeof YandexTaxiRequestSearchQuery>;

export const YandexTaxiUpdateQuery = t.type({
  factCost: t.number,
  status: t.string,
  reason: t.string,
});
export type YandexTaxiUpdateQuery = t.TypeOf<typeof YandexTaxiUpdateQuery>;

export const YandexTaxiLimitAllowance = t.type({
  order: t.boolean,
});

export type YandexTaxiLimitAllowance = t.TypeOf<typeof YandexTaxiLimitAllowance>;

export const YandexTaxiExternalTariff = t.type({
  price: t.number,
  distant: t.number,
  type: t.string,
  waitTime: t.string,
  time: t.string,
});

export type YandexTaxiExternalTariff = t.TypeOf<typeof YandexTaxiExternalTariff>;

export const YandexTariffResponseType = t.array(YandexTaxiExternalTariff);
export type YandexTariffResponseType = t.TypeOf<typeof YandexTariffResponseType>;
