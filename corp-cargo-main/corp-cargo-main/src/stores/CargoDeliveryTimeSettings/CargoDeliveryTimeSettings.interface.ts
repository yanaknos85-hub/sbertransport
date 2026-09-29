import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

/**
 * Константа для типа настроек сроков доставки
 */
export const CargoDeliveryTimeSettings = t.type({
  id: tt.uuid,
  label: t.string,
  urgency: t.string,
  start: t.number,
  end: t.number,
  defaultValue: t.number,
  value: t.number,
});

/**
 * Массив констант настроек сроков доставки
 */
export const CargoDeliveryTimeSettingsArray = t.array(CargoDeliveryTimeSettings);

/**
 * Тип для CargoDeliveryTimeSettings
 */
export type CargoDeliveryTimeSettingsType = t.TypeOf<typeof CargoDeliveryTimeSettings>;

/**
 * Тип для CargoDeliveryTimeSettingsArray
 */
export type CargoDeliveryTimeSettingsArrayType = t.TypeOf<typeof CargoDeliveryTimeSettingsArray>;
