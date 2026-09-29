import { useForm } from 'antd/lib/form/Form';
import {
  useCallback, useEffect, useMemo
} from 'react';
import { ContractRestrictionTypes, DATE_FORMAT, VatValue } from 'constants/constants.app';
import { Contract } from 'stores/Contracts/Contracts.interface';
import { useCreateContract, useUpdateContract } from 'api/contracts';
import moment, { Moment } from 'moment';
import { useProfile } from 'api/profile';
import { ignore } from 'utils';
import { useModal } from '../../context/modal.context';
import { serviceTypesDefaultValuePassengers } from '../../constants/constants';
import { UUID } from 'utils/io-ts';

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

  // Обновляем дофолтные значении при обновлении данных об открытом контрагенте
  const initialValues: Omit<ContractFields, 'organizationIds'> & { organizationIds?: UUID } = useMemo(
    () => {
      const values = {
        ...contract!,
        organizationIds: contract?.organizationIds?.[0],
        serviceType: serviceTypesDefaultValuePassengers.value,
        period: createDateRange(contract),
        restrictionType: contract?.restrictionType ?? ContractRestrictionTypes.NONE,
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

  const saveForm = useCallback(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (values: any) => {
      const {
        period, vatValue, ...other
      } = values;
      const [startDate, endDate] = (period || []) as [moment.Moment, moment.Moment];
      const { vatValue: initVatValue, ...otherInitialValues } = initialValues;

      const getContractValues = () => {
        return {
          ...otherInitialValues,
          ...other,
          organizationIds: other.organizationIds ? [other.organizationIds] : undefined,
          includeVat: vatValue !== VatValue.NULL,
          startDate: startDate?.format(DATE_FORMAT.BASE) ?? initialValues.startDate,
          endDate: endDate?.format(DATE_FORMAT.BASE) ?? initialValues.endDate ?? null,
          ...(vatValue !== VatValue.ZERO ? { vatValue } : {}),
          restrictedIds: other.restrictionType === ContractRestrictionTypes.BLACK_LIST
            ? other.restrictedIds
            : undefined,
          vatValue: vatValue !== VatValue.NULL ? +vatValue : null,
        };
      };

      const saveContract = modalState.type === 'edit' ? editContract : createContract;
      saveContract(getContractValues()).then(closeModal).catch(ignore);
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [
      initialValues,
      modalState.type,
      editContract,
      createContract,
      organizationId,
      closeModal,
    ]
  );

  return {
    form,
    saveForm,
    initialValues,
  };
};
