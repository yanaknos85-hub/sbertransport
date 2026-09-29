import { useForm } from 'antd/lib/form/Form';
import {
  useCallback, useEffect, useMemo, useState
} from 'react';
import { DATE_FORMAT, VatValue } from 'constants/constants.app';
import { Contract } from 'stores/Contracts/Contracts.interface';
import { useCreateContract, useUpdateContract } from 'api/contracts';
import { useContractors } from 'api/contractors';
import moment, { Moment } from 'moment';
import { useProfile } from 'api/profile';
import { ignore } from 'utils';
import { useModal } from '../../context/modal.context';
import { serviceTypesDefaultValuePassengers } from '../../constants/constants';

type DateRange = [Moment, Moment | undefined] | [Moment] | undefined;
type ContractFields = Omit<Contract, 'vatValue'> & {
  serviceType: string;
  period: DateRange;
  service?: string;
  vatValue?: VatValue | string;
};

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
  // export const useModalForm = (contract: Contract | undefined, mode: Modes) => {
  const [form] = useForm();

  const { modalState, closeModal } = useModal();
  const { organizationId } = useProfile().data;

  // ПОЛЯ ФОРМЫ
  const [vatChecked, setVatChecked] = useState<boolean>(contract?.includeVat ?? false);

  // Обновляем дофолтные значении при обновлении данных об открытом контрагенте
  const initialValues: ContractFields = useMemo(
    () => {
      const values = {
        ...contract!,
        organizationIds: contract?.organizationIds,
        serviceType: serviceTypesDefaultValuePassengers.value,
        period: createDateRange(contract),
        driverLatePickupPenalty: modalState.type === 'edit' ? contract?.driverLatePickupPenalty : 0.1,
        poorServiceQualityPenalty: modalState.type === 'edit' ? contract?.poorServiceQualityPenalty : 0.1,
        driverOrderCancellationPenalty: modalState.type === 'edit' ? contract?.driverOrderCancellationPenalty : 0.1,
        vatValue: contract
          ? contract?.includeVat ? String(contract.vatValue) as VatValue : VatValue.NULL
          : String(VatValue.BASE_22),
      };

      return values;
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [contract]
  );

  useEffect(() => {
    form.resetFields();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialValues, form.resetFields, modalState.type]);

  // СОХРАНЕНИЕ ФОРМЫ

  const [createContract] = useCreateContract();
  const [editContract] = useUpdateContract();

  const { data: { contractors } } = useContractors();

  const saveForm = useCallback(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (values: any) => {
      const {
        period, vatValue, ...other
      } = values;
      const [startDate, endDate] = period || [];
      const { vatValue: initVatValue, ...otherInitialValues } = initialValues;

      const getContractValues = () => {
        return {
          ...otherInitialValues,
          ...other,
          includeVat: vatValue !== VatValue.NULL,
          startDate: startDate?.format(DATE_FORMAT.BASE) ?? initialValues.startDate,
          endDate: endDate?.format(DATE_FORMAT.BASE) ?? initialValues.endDate ?? null,
          vatValue: vatValue !== VatValue.NULL ? +vatValue : null,
        };
      };

      const saveContract = modalState.type === 'edit' ? editContract : createContract;
      const result = saveContract(getContractValues());
      if (result && typeof result.then === 'function') {
        result.then(closeModal).catch(ignore);
      }
      return result;
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [
      initialValues,
      vatChecked,
      modalState.type,
      editContract,
      createContract,
      organizationId,
      closeModal,
      contractors,
    ]
  );

  return {
    form,
    saveForm,
    initialValues,
    vatChecked,
    setVatChecked,
  };
};
