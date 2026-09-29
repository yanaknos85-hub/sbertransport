import {
  useCallback, useEffect, useMemo, useState
} from 'react';
import { useParams } from 'react-router-dom';
import { useForm } from 'antd/lib/form/Form';

import { useCreateTariff, useUpdateTariff } from 'api/tariffs';
import { useProfile } from 'api/profile';
import { TariffTypes } from 'constants/constants.app';
import { fillFormOnCreate, fillFormOnUpdate, prepareToSubmitData } from 'modules/NewTariffs/utils/utils';
import { StoreNames, useAppStore } from 'stores';
import { TariffJson } from 'stores/Tariffs/Tariffs.interface';
import { ignore } from 'utils';

import { useModal } from '../../context/modal.context';

export const useModalForm = (tariff: TariffJson | undefined) => {
  const [form] = useForm();
  const { [StoreNames.tariffsStore]: tariffsStore } = useAppStore();
  const { organizationId: userOrganizationId } = useProfile(true).data;

  const { modalState, closeModal } = useModal();

  // ПОЛЯ ФОРМЫ

  const [transportValue, setTransportValue] = useState(tariff?.transportType || '');
  const [groupTransferClass, setGroupTransferClass] = useState(tariff?.groupTransferClass || '');
  const [selectedOrganizationId, setSelectedOrganizationId] = useState('');
  const [transportTypeDisabled, setTransportTypeDisabled] = useState(true);

  // Обновляем дефолтные значении при обновлении данных об открытом контрагенте
  const initialValues: TariffJson = useMemo(
    () => ({
      ...tariff!,
      regionId: tariff?.regionIds || tariff?.regionId || [],
    }),
    [tariff]
  );

  useEffect(() => {
    form.resetFields();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialValues, form.resetFields, modalState.type]);

  useEffect(() => {
    initialValues.id
      ? fillFormOnUpdate(form, initialValues, transportValue)
      : fillFormOnCreate(form, transportValue);
  }, [initialValues, form, transportValue, modalState.type]);

  useEffect(() => {
    setTransportValue(tariff?.transportType || '');
    setGroupTransferClass(tariff?.groupTransferClass || '');
  }, [tariff]);

  // СОХРАНЕНИЕ ФОРМЫ

  const [createTariff] = useCreateTariff();
  const [editTariff] = useUpdateTariff();

  const { tariffType } = useParams<{ tariffType: TariffTypes }>();

  const saveForm = useCallback(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (data: any) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const saveTariff: (values: any) => Promise<any>
        = modalState.type === 'edit'
          ? editTariff
          : createTariff;

      saveTariff({
        tariff: prepareToSubmitData(
          {
            ...initialValues,
            ...data,
            ...(tariffType === TariffTypes.OUTCOME ? { organizationId: userOrganizationId } : {}),
            regionId: typeof data.regionId === 'string' ? [data.regionId] : data.regionId,
            contractType: tariffType,
          },
          modalState.type === 'edit' ? (modalState.id as string) : undefined,
          data.transportType
        ),
        transTypeId: data.transportType.toLowerCase(),
        tariffId: modalState.type === 'edit' ? (modalState.id as string) : undefined,
        refetchTariffs: () => tariffsStore.refetchSDOTariffs(tariffType),
      })
        .then(closeModal)
        .catch(ignore);
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [
      modalState,
      editTariff,
      createTariff,
      closeModal,
      initialValues,
      tariffType,
    ]
  );

  return {
    form,
    saveForm,
    initialValues,
    transportValue,
    setTransportValue,
    groupTransferClass,
    setGroupTransferClass,
    selectedOrganizationId,
    setSelectedOrganizationId,
    transportTypeDisabled,
    setTransportTypeDisabled,
  };
};
