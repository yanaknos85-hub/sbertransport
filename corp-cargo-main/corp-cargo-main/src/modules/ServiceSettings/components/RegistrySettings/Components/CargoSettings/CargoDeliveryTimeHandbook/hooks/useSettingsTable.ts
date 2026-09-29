import { CargoDeliveryTimeSettingsArrayType } from 'stores/CargoDeliveryTimeSettings/CargoDeliveryTimeSettings.interface';
import { useColumns } from './useColumns';
import { DeliveryRange, DeliveryTimeRow, DeliveryUrgency } from '../types/types';

const mapDeliveryTimesToRow = (
  urgency: DeliveryUrgency,
  cargoDeliveryTimeSettingsArray: CargoDeliveryTimeSettingsArrayType,
  valueType: 'value' | 'default'
): DeliveryTimeRow => {
  const row: DeliveryTimeRow = {
    urgency,
  };
  cargoDeliveryTimeSettingsArray
    .filter(item => item.urgency === urgency)
    .forEach(item => (row[item.start as DeliveryRange] = valueType === 'value' ? item.value : item.defaultValue));

  return row;
};

export const useSettingsTable = (cargoDeliveryTimeSettingsArray: CargoDeliveryTimeSettingsArrayType) => {
  const columns = useColumns();
/* Скрыто в рамках задачи TRANSPORT-24303 */
  const currentValues = [
    mapDeliveryTimesToRow(DeliveryUrgency.STANDART, cargoDeliveryTimeSettingsArray, 'value'),
    // mapDeliveryTimesToRow(DeliveryUrgency.EXPRESS, cargoDeliveryTimeSettingsArray, 'value'),
  ];
  const defaultValues = [
    mapDeliveryTimesToRow(DeliveryUrgency.STANDART, cargoDeliveryTimeSettingsArray, 'default'),
    // mapDeliveryTimesToRow(DeliveryUrgency.EXPRESS, cargoDeliveryTimeSettingsArray, 'default'),
  ];

  return {
    currentValues,
    defaultValues,
    columns,
  };
};
