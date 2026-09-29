import * as t from 'io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export const SortSettings = t.intersection([
  t.type({
    sorted: t.boolean,
    unsorted: t.boolean,
    empty: t.boolean,
  }),
  t.partial({
    property: t.string,
    directionAsc: t.boolean,
  }),
]);
export type SortSettings = t.TypeOf<typeof SortSettings>;

export enum SortFields {
  humanReadableId = 'humanReadableId',
  expectedDistance = 'expectedDistance',
  expectedCost = 'expectedCost',
  creationTime = 'creationTime',
  deadline = 'deadline',
}

export enum SortDirection {
  ASC = 'ASC',
  DESC = 'DESC',
  ASCEND = 'ascend',
  DESCEND = 'descend',
}

export const SortSetting = t.strict({
  sortField: ioTypeFromEnum<SortFields>('SortFields', SortFields),
  sortDirection: t.boolean,
  directionAsc: t.boolean,
  property: t.string,
});

export type SortSetting = t.TypeOf<typeof SortSetting>;
