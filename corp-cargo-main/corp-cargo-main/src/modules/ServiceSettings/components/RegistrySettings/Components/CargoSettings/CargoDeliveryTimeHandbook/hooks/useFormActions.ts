import { FormInstance } from 'antd/es/form';
import { Dispatch, SetStateAction } from 'react';
import { useGetCargoDeliveryTimeSettings, useUpdateCargoDeliveryTimeSettings } from 'api/cargo-delivery-time-settings';
import { fillFormInitialValues, prepareToSubmitForm } from '../utils/utils';

interface Export {
  onSave: () => void;
  onCancel: () => void;
}

export const useFormActions = (form: FormInstance, setBusy: Dispatch<SetStateAction<boolean>>): Export => {
  const { cargoDeliveryTimeSettingsArray } = useGetCargoDeliveryTimeSettings().data;
  const [updateSettings] = useUpdateCargoDeliveryTimeSettings();

  /**
   * Действия по нажатию на кнопку Сохранить
   */
  const onFinish = () => {
    setBusy(true);
    const data = form.getFieldsValue();
    const submitted = prepareToSubmitForm(data, cargoDeliveryTimeSettingsArray);
    updateSettings({
      cargoDeliveryTimeSettingsArray: submitted,
    }).finally(() => setBusy(false));
  };

  /**
   * Действия по нажатию на кнопку Отменить
   */
  const onCancel = () => {
    fillFormInitialValues(form, cargoDeliveryTimeSettingsArray);
  };

  return {
    onSave: onFinish,
    onCancel,
  };
};
