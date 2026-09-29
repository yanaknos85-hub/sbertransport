import { useForm } from 'antd/lib/form/Form';
import {
  useCallback, useEffect, useMemo, useState
} from 'react';
import { DATE_FORMAT } from 'constants/constants.app';
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

  // ПОЛЯ ФОРМЫ
  const [templateChecked, setTemplateChecked] = useState<boolean>(contract?.includeTemplate ?? false);
  const [purposeChecked, setPurposeChecked] = useState<boolean>(contract?.includePurpose ?? false);

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
      };

      return values;
    },
    [contract]
  );

  useEffect(() => {
    form.resetFields();
  }, [initialValues, form.resetFields, modalState.type]);

  useEffect(() => {
    setTemplateChecked(contract?.includeTemplate ?? false);
    setPurposeChecked(contract?.includePurpose ?? false);

    if (contract?.includeTemplate && contract?.includePurpose) {
      setPurposeChecked(false);
    }
  }, [contract]);

  // СОХРАНЕНИЕ ФОРМЫ

  const [createContractCargo] = useCreateContractCargo();
  const [editContractCargo] = useUpdateContractCargo();

  const { data: { contractors } } = useContractors();

  const saveForm = useCallback(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (values: any) => {
      const {
        period, vatValue, template, purpose, ...other
      } = values;
      const [startDate, endDate] = (period || []) as [moment.Moment, moment.Moment];
      const { vatValue: initVatValue, templateValue: initTemplateValue, ...otherInitialValues } = initialValues;

      const organizationIds = form.getFieldValue('organizationIds') ?? [organizationId];

      /// Нужна доработка бэка, чтобы не было этой конструкции!
      const settings = [
        { type: 'CARGO_ROUTE_START_PLANNING_DEADLINE', value: moment(values.CARGO_ROUTE_START_PLANNING_DEADLINE).format(DATE_FORMAT.TIME_BASE_SHORT) },
        { type: 'CARGO_ROUTE_BACK_PLANNING_DEADLINE', value: moment(values.CARGO_ROUTE_BACK_PLANNING_DEADLINE).format((DATE_FORMAT.TIME_BASE_SHORT)) },
        { type: 'CARGO_ROUTE_FINISH_PLANNING_DEADLINE', value: moment(values.CARGO_ROUTE_FINISH_PLANNING_DEADLINE).format((DATE_FORMAT.TIME_BASE_SHORT)) },
      ];

      const getContractValues = () => {
        return {
          ...otherInitialValues,
          ...other,
          period: undefined, // на бэке это поле не нужно, все данные передаются в startDate и endDate
          includeTemplate: templateChecked,
          includePurpose: purposeChecked,
          startDate: startDate?.format(DATE_FORMAT.BASE) ?? initialValues.startDate,
          endDate: endDate?.format(DATE_FORMAT.BASE) ?? initialValues.endDate ?? null,
          organizationIds: organizationIds,
          settings,
          ...(vatValue !== undefined ? { vatValue } : {}),
          ...(templateChecked ? { template } : {}),
          ...(purposeChecked ? { purpose } : {}),

        };
      };

      const saveContract = modalState.type === 'edit' ? editContractCargo : createContractCargo;
      saveContract(getContractValues()).then(closeModal).catch(ignore);
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [
      initialValues,
      templateChecked,
      purposeChecked,
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
    templateChecked,
    setTemplateChecked,
    purposeChecked,
    setPurposeChecked
  };
};
