import { useForm } from 'antd/lib/form/Form';
import {
  useCallback, useEffect, useMemo, useState
} from 'react';
import { TariffJson } from 'stores/Tariffs/Tariffs.interface';
import { useDepartmentsByNameSubstring } from 'api/departments';
import { useCreateTariff, useUpdateTariff } from 'api/tariffs';
import { fillFormOnCreate, fillFormOnUpdate, prepareToSubmitData } from 'modules/NewTariffs/utils/utils';
import { ignore } from 'utils';
import { UUID } from 'utils/io-ts';
import { useModal } from '../../context/modal.context';
import { StoreNames, useAppStore } from 'stores';

export const useModalForm = (tariff: TariffJson | undefined) => {
  const [form] = useForm();
  const { [StoreNames.tariffsStore]: tariffsStore } = useAppStore();

  const { modalState, closeModal } = useModal();

  // ПОЛЯ ФОРМЫ

  const [transportValue, setTransportValue] = useState(tariff?.transportType || '');
  const [groupTransferClass, setGroupTransferClass] = useState(tariff?.groupTransferClass || '');
  const [selectedOrganizationId, setSelectedOrganizationId] = useState('');
  const [departmentsSearchString, setDepartmentsSearchString] = useState('');
  const [transportTypeDisabled, setTransportTypeDisabled] = useState(true);
  const [contractorIdDisabled, setContractorIdDisabled] = useState(true);

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

  const departmentOrgId = initialValues?.organizationId ?? selectedOrganizationId;
  const departmentsByNameSubstring = useDepartmentsByNameSubstring(departmentOrgId as UUID, departmentsSearchString, {
    suspense: false,
    enabled: departmentsSearchString && departmentOrgId,
  }).data;
  const departmentsByNameSubstringOptions = useMemo(() => (
    departmentsByNameSubstring
      ? departmentsByNameSubstring.departmentsByNameSubstringResponse.content?.map(({ id: value, code: label }) => ({
        label,
        value,
      }))
      : []
  ), [departmentsByNameSubstring]
  );

  // СОХРАНЕНИЕ ФОРМЫ

  const [createTariff] = useCreateTariff();
  const [editTariff] = useUpdateTariff();

  const saveForm = useCallback(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (data: any) => {
      if (data.departmentId) {
        if (departmentsByNameSubstring) {
          data.code = departmentsByNameSubstringOptions.find(opt => opt.value === data.departmentId)?.label;
          data.departmentHumanReadableId = departmentsByNameSubstring.departmentsByNameSubstringResponse.content.find(
            dep => dep.id === data.departmentId
          )?.humanReadableId;
        } else {
          data.departmentId = initialValues?.department?.id;
          data.departmentHumanReadableId = initialValues?.department?.humanReadableId;
          data.code = initialValues?.department?.code;
        }
      }

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
            regionId: data.regionIds || data.regionId,
            regionIds: data.regionId ?? data.regionIds,
          },
          modalState.type === 'edit' ? (modalState.id as string) : undefined,
          data.transportType
        ),
        transTypeId: data.transportType.toLowerCase(),
        tariffId: modalState.type === 'edit' ? (modalState.id as string) : undefined,
        refetchTariffs: tariffsStore.refetchFilteredTariffs,
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
      departmentsByNameSubstring,
      departmentsByNameSubstringOptions,
      initialValues,
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
    departmentsSearchString,
    setDepartmentsSearchString,
    transportTypeDisabled,
    setTransportTypeDisabled,
    contractorIdDisabled,
    setContractorIdDisabled,
    departmentsByNameSubstringOptions,
  };
};
