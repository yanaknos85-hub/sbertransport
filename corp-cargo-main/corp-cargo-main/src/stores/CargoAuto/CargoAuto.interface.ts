import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from '../../utils/ioTypeFromEnum';

export enum CategoryCargoEnum {
  REGULAR = 'REGULAR',
  OTHER = 'OTHER',
  LIQUID = 'LIQUID',
  BULK = 'BULK',
  CORRESPONDENCE = 'CORRESPONDENCE',
}

export const CategoryCargo = {
  [CategoryCargoEnum.OTHER]: 'Другое',
  [CategoryCargoEnum.REGULAR]: 'Обычные',
  [CategoryCargoEnum.LIQUID]: 'Наливные грузы',
  [CategoryCargoEnum.BULK]: 'Насыпные грузы',
  [CategoryCargoEnum.CORRESPONDENCE]: 'Корреспонденция',
};

export const TypeCargo = ioTypeFromEnum<CategoryCargoEnum>('CategoryCargoEnum', CategoryCargoEnum);

export const CargoAuto = t.intersection([
  t.type({
    id: tt.uuid,
    name: t.string,
    volume: t.number,
    capacity: t.type({
      id: t.string,
      capacity: t.number,
    }),
    cargoCategory: t.string,
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

export const CategoryCargoAuto = t.type({
  value: t.string,
  description: TypeCargo,
});

export const CargoAutoArray = t.array(CargoAuto);
export const CargoAutoCapacity = t.array(AutoCapacity);
export const CargoAutoCategory = t.array(CategoryCargoAuto);

export type AutoCapacity = t.TypeOf<typeof AutoCapacity>;
export type CargoAuto = t.TypeOf<typeof CargoAuto>;
export type CategoryCargoAuto = t.TypeOf<typeof CategoryCargoAuto>;
