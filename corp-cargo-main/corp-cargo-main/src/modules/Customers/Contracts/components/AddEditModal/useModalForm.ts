/* eslint-disable no-unused-vars, @typescript-eslint/no-unused-vars */
import { useForm } from 'antd/lib/form/Form';
import {
  useCallback, useEffect, useMemo
} from 'react';
import { useParams } from 'react-router-dom';
import {
  ContractRestrictionTypes, ContractTypes, DATE_FORMAT, VatValue
} from 'constants/constants.app';
import { Contract } from 'stores/Contracts/Contracts.interface';
import { useContractors } from 'api/contractors';
import { useCreateContractCargo, useUpdateContractCargo } from 'api/contracts';
import moment, { Moment } from 'moment';
import { useProfile } from 'api/profile';
import { ignore } from 'utils';
import { useModal } from '../../context/modal.context';
import { serviceTypesDefaultValueCargo } from '../../constants/constants';

type DateRange = [Moment, Moment | undefined] | [Moment] | undefined;

const createDateRange = (contract: Contract | undefined): DateRange => {
  const start = contract?.startDate;
  const end = contract?.endDate;
  const stringToDate = (str: string) => moment(str, DATE_FORMAT.BASE);

  return start
    ? end
      ? [stringToDate(start), stringToDate(end)]
      : [stringToDate(start)]
    : undefined;
};

export const useModalForm = (contract: Contract | undefined) => {
  const [form] = useForm();

  const { modalState, closeModal } = useModal();
  const { organizationId } = useProfile().data;
  const { contractType } = useParams<{ contractType: ContractTypes }>();

  // Отображение значений полей для массива settings для Параметров договора
  const contractParams = contract?.settings?.reduce((acc, setting) => {
    acc[setting.type] = moment(setting.value, DATE_FORMAT.TIME_BASE_SHORT);
    return acc;
  }, {});

  // Обновляем дефолтные значения при обновлении данных об открытом контрагенте
  const initialValues = useMemo(
    () => {
      const values = {
        ...contract!,
        ...contractParams,
        organizationIds: contract?.organizationIds,
        serviceType: serviceTypesDefaultValueCargo.value,
        period: createDateRange(contract),
        restrictionType: contract?.restrictionType ?? ContractRestrictionTypes.NONE,
        vatValue: contract?.includeVat ? contract.vatValue : VatValue.ZERO,
      };

      return values;
    },
    [contract]
  );

  useEffect(() => {
    form.resetFields();
  }, [initialValues, form.resetFields, modalState.type]);

  // СОХРАНЕНИЕ ФОРМЫ

  const [createContractCargo] = useCreateContractCargo();
  const [editContractCargo] = useUpdateContractCargo();

  const { data: { contractors } } = useContractors();

  const saveForm = useCallback(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (values: any) => {
      const {
        period, vatValue, ...other
      } = values;
      const [startDate, endDate] = (period || []) as [moment.Moment, moment.Moment];
      const { vatValue: initVatValue, ...otherInitialValues } = initialValues;

      // const organizationIds = form.getFieldValue('organizationIds') ?? [organizationId]; TODO временно не используется

      const getContractValues = () => {
        return {
          ...otherInitialValues,
          ...other,
          period: undefined, // на бэке это поле не нужно, все данные передаются в startDate и endDate
          includeVat: vatValue !== VatValue.ZERO,
          startDate: startDate?.format(DATE_FORMAT.BASE) ?? initialValues.startDate,
          endDate: endDate?.format(DATE_FORMAT.BASE) ?? initialValues.endDate ?? null,
          ...(vatValue !== VatValue.ZERO ? { vatValue } : {}),
          contractType,
          restrictedIds: other.restrictionType === ContractRestrictionTypes.BLACK_LIST
          || other.restrictionType === ContractRestrictionTypes.SELECT_LIST
            ? other.restrictedIds
            : undefined,
        };
      };

      const saveContract = modalState.type === 'edit' ? editContractCargo : createContractCargo;
      saveContract(getContractValues()).then(closeModal).catch(ignore);
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [
      initialValues,
      modalState.type,
      editContractCargo,
      createContractCargo,
      organizationId,
      closeModal,
      contractors,
    ]
  );

  return {
    form,
    saveForm,
    initialValues,
  };
};
