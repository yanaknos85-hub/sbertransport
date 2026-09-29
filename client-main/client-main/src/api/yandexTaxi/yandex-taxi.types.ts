import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { YandexTaxiTariff } from './yandex-taxi.constants';
import { Waypoint } from 'stores/Request/Response/types';

const SortDirection = t.keyof({
  DESC: null,
  ASC: null,
});
export type SortDirection = t.TypeOf<typeof SortDirection>;

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
