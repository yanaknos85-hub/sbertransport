import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

import { TaxiClass } from 'stores/Trip/Trip.interface';

export const AvailableTaxiClasses = t.array(ioTypeFromEnum<TaxiClass>('TaxiClassEnum', TaxiClass));

export type AvailableTaxiClasses = t.TypeOf<typeof AvailableTaxiClasses>;

export const Position = t.intersection([
  t.type({
    id: tt.uuid,
    organizationId: tt.uuid,
    positionName: t.string,
    humanReadableId: t.string,
  }),
  t.partial({
    selfApproved: t.boolean,
    availableClasses: AvailableTaxiClasses,
    active: t.boolean,
  }),
]);

export type Position = t.TypeOf<typeof Position>;
