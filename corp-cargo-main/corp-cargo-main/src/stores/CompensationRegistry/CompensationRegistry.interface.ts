import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const DateRangeISO = t.strict({
  start: t.string,
  end: t.string,
});

export type DateRangeISO = t.TypeOf<typeof DateRangeISO>;

export const SortSettings = t.type({
  property: t.string,
  directionAsc: t.boolean,
});
export type SortSettings = t.TypeOf<typeof SortSettings>;

export const Filters = t.intersection([
  t.type({
  }),
  t.partial({
    humanReadableId: t.string,
    requestStatusSet: t.array(t.string),
    approvalDateRange: DateRangeISO,
    creationDate: DateRangeISO,
    pageSetting: t.type({ page: t.number, size: t.number }),
    sortSetting: SortSettings,
    organizationId: tt.uuid,
    department1: t.array(t.string),
    department2: t.array(t.string),
    department3: t.array(t.string),
    department4: t.array(t.string),
    department5: t.array(t.string),
    department6: t.array(t.string),
  }),
]);
export type CargoCompensationRegistryFilters = t.TypeOf<typeof Filters>;
