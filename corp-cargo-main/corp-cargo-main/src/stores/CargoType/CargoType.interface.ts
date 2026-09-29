import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const CargoType = t.type({
  active: t.boolean,
  category: t.string,
  height: t.number,
  id: tt.uuid,
  length: t.number,
  name: t.string,
  type: t.string,
  volume: t.number,
  weight: t.number,
  width: t.number,
  accessLevel: t.string,
  organizationId: t.string,
});

export const CargoTypeArray = t.array(CargoType);

export type CargoType = t.TypeOf<typeof CargoType>;

export enum CargoCategory {
  BULK = 'BULK',
  OTHER = 'OTHER',
  LIQUID = 'LIQUID',
  REGULAR = 'REGULAR',
  CORRESPONDENCE = 'CORRESPONDENCE',
}
