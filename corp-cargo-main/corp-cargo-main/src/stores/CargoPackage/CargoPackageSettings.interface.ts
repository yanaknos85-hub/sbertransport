import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

/**
 * Константа для типа настроек упаковок
 */
export const CargoPackageSettings = t.type({
  id: tt.uuid,
  label: t.string,
  cost: t.number,
  unit: t.string,
});

/**
 * Массив констант настроек упаковок
 */
export const CargoPackageSettingsArray = t.array(CargoPackageSettings);

/**
 * Тип для CargoPackageSettings
 */
export type CargoPackageSettingsType = t.TypeOf<typeof CargoPackageSettings>;

/**
 * Тип для CargoPackageSettings с удаленными полями id и isNew
 */
export type SaveCargoPackageSettingsType = Omit<CargoPackageSettingsType, 'id' | 'isNew'>;

/**
 * Расширенный интерфейс для использования в таблице с указанием признака, сохранена ли эта строка в БД
 */
export interface CargoPackageSettingsRecord extends CargoPackageSettingsType {
  isNew: boolean;
}
