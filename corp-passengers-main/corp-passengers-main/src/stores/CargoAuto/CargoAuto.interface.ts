import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const CargoAuto = t.intersection([
  t.type({
    id: tt.uuid,
    name: t.string,
    volume: t.number,
    capacity: t.type({
      id: t.string,
      capacity: t.number,
    }),
  }),
  t.partial({
    length: t.number,
    width: t.number,
    height: t.number,
  }),
]);

export const AutoCapacity = t.type({
  id: t.string,
  capacity: t.number,
});

export const CargoAutoArray = t.array(CargoAuto);
export const CargoAutoCapacity = t.array(AutoCapacity);

export type AutoCapacity = t.TypeOf<typeof AutoCapacity>;
export type CargoAuto = t.TypeOf<typeof CargoAuto>;
