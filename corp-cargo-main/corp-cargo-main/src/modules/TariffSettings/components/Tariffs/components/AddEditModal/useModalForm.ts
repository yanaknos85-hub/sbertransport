import { useForm } from 'antd/lib/form/Form';
import {
  useCallback, useEffect, useMemo, useState
} from 'react';
import { TariffJson } from 'stores/Tariffs/Tariffs.interface';
import { useDepartmentsByNameSubstring } from 'api/departments';
import { useCreateTariff, useUpdateTariff } from 'api/tariffs-cargo';
import { fillFormOnCreate, fillFormOnUpdate, prepareToSubmitData } from 'modules/NewTariffs/utils/utils';
import { ignore } from 'utils';
import { UUID } from 'utils/io-ts';
import { useModal } from '../../context/modal.context';

export const useModalForm = (tariff: TariffJson | undefined) => {
  const [form] = useForm();

  const { modalState, closeModal } = useModal();

  // ПОЛЯ ФОРМЫ

  const [transportValue, setTransportValue] = useState(tariff?.transportType || '');
  const [selectedOrganizationId, setSelectedOrganizationId] = useState('');
  const [departmentsSearchString, setDepartmentsSearchString] = useState('');
  const [transportTypeDisabled, setTransportTypeDisabled] = useState(true);
  const [contractorIdDisabled, setContractorIdDisabled] = useState(true);

  // Обновляем дефолтные значении при обновлении данных об открытом контрагенте
  const initialValues: TariffJson = useMemo(
    () => ({
      ...tariff!,
    }),
    [tariff]
  );

  useEffect(() => {
    form.resetFields();
  }, [initialValues, form.resetFields, modalState.type]);

  useEffect(() => {
    initialValues.id
      ? fillFormOnUpdate(form, initialValues, transportValue)
      : fillFormOnCreate(form, transportValue);
  }, [initialValues, form, transportValue, modalState.type]);

  useEffect(() => {
    setTransportValue(tariff?.transportType || '');
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

      const saveTariff: (values: any) => Promise<any>
        = modalState.type === 'edit'
          ? editTariff
          : createTariff;

      saveTariff({
        tariff: prepareToSubmitData(
          { ...initialValues, ...data, calculationType: data.calculationType },
          modalState.type === 'edit' ? (modalState.id as string) : undefined,
          data.transportType
        ),
        transTypeId: data.transportType.toLowerCase(),
        tariffId: modalState.type === 'edit' ? (modalState.id as string) : undefined,
      })
        .then(closeModal)
        .catch(ignore);
    },
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
