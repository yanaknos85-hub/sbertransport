import { Dispatch, SetStateAction, useState } from 'react';
import { FormInstance } from 'antd/es/form';
import { useForm } from 'antd/lib/form/Form';
import { useGetCargoDeliveryTimeSettings } from 'api/cargo-delivery-time-settings';
import { CargoDeliveryTimeSettingsArrayType } from 'stores/CargoDeliveryTimeSettings/CargoDeliveryTimeSettings.interface';

export const useCargoDeliveryTimeSettingsForm = (): {
  form: FormInstance;
  cargoDeliveryTimeSettingsArray: CargoDeliveryTimeSettingsArrayType;
  busy: boolean;
  setBusy: Dispatch<SetStateAction<boolean>>;
} => {
  const [form] = useForm();
  const { cargoDeliveryTimeSettingsArray } = useGetCargoDeliveryTimeSettings().data;
  const [busy, setBusy] = useState(true);

  return {
    form,
    cargoDeliveryTimeSettingsArray,
    busy,
    setBusy,
  };
};
