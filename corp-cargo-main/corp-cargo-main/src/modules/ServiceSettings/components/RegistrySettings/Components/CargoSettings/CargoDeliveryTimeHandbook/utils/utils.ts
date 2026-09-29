import { FormInstance } from 'antd/es/form';
import { CargoDeliveryTimeSettingsArrayType } from 'stores/CargoDeliveryTimeSettings/CargoDeliveryTimeSettings.interface';
import { UUID } from 'utils/io-ts';
import { DeliveryUrgency } from '../types/types';

interface SettingsFormData {
  id: UUID;
  value: number;
}

/**
 * Подготавливаем массив CargoDeliveryTimeSettingsArrayType для отпрвавки на рест
 * @param data массив данных с формы
 * @param cargoDeliveryTimeSettingsArray {@link CargoDeliveryTimeSettingsArrayType}
 */
export const prepareToSubmitForm = (
  data: Record<string, number>,
  cargoDeliveryTimeSettingsArray: CargoDeliveryTimeSettingsArrayType
): CargoDeliveryTimeSettingsArrayType => {
  const preparedDeliveryTimeSettings = cargoDeliveryTimeSettingsArray
  .filter(settingsEntry => settingsEntry.urgency === DeliveryUrgency.STANDART)
  .map(settingsEntry => ({
    ...settingsEntry,
    value: data[`${settingsEntry.urgency}_${settingsEntry.start}`],
  }));

  return preparedDeliveryTimeSettings;
};

/**
 * Заполняем форму текущими значениями
 * @param form {@link FormInstance} экрнанная форма
 * @param settings {@link CargoDeliveryTimeSettingsArrayType} массив настроек сроков доставки
 */
export const fillFormInitialValues = (form: FormInstance, settings: CargoDeliveryTimeSettingsArrayType): void => {
  settings.forEach(item => {
    form.setFieldsValue({
      [`${item.urgency}_${item.start}`]: item.value,
    });
  });
};
