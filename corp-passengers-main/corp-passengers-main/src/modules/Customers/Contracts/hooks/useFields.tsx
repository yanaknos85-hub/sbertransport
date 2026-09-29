import { useEffect, useMemo, useRef } from 'react';
import { useParams } from 'react-router-dom';

import { useTranslation } from 'i18n';
import { useTransportTypes } from 'api/transport-types';
import { useServiceTypes } from 'api/service-types';
import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { useContractors } from 'api/contractors';
import { useGetListRegions } from 'api/tariffs';
import { LabeledValue, preventDefault } from 'utils';
import {
  MAX_TEXT_INPUT, MIN_TEXT_INPUT, serviceTypesDefaultValuePassengers
} from '../constants/constants';
import { useModal } from '../context/modal.context';
import { useOrganizationProjection } from 'api/organizations/search';
import { ValidationRules } from 'shared/fieldValidationRules';
import { ContractRestrictionTypes, ContractTypes, vatValueTitles } from 'constants/constants.app';
import { formatContractText } from '../utils/formatContractText';

const vatValueOptions = Object.entries(vatValueTitles).map(([value, label]) => ({ value: +value, label }));

export const useFields = ({ ...rest }: {
  isCreation?: boolean;
  modalStateType?: string;
  regionIds: string[];
}) => {
  const { modalState } = useModal();
  const { t } = useTranslation();
  const regionIdsRef = useRef<string[]>([]);

  const { contractType } = useParams<{ contractType: ContractTypes }>();

  const {
    isCreation, regionIds = [],
  } = rest;

  const organizations = useOrganizationProjection({}, {
    enabled: modalState.type !== null,
    suspense: false,
  }).data;

  // запросы для селектов
  const transportTypes = useTransportTypes({
    suspense: false,
    enabled: modalState.type !== null,
  }).data?.filter(({ name }) => contractType ? name === TransportTypes.TAXI : true)
    .map(({ name, rusName }) => ({ value: name, label: rusName })) ?? [];

  const serviceTypes = useServiceTypes({ suspense: false }).data?.map(s => ({
    value: s.value,
    label: s.name,
  })) ?? [];

  // ДОБАВИТЬ ПАГИНАЦИЮ
  const {
    data: contractorsData,
    isFetching: isContractorsLoading,
    refetch,
  } = useContractors({
    params: { regionIds },
    config: {
      enabled:
        modalState.type !== null
        && ((contractType === ContractTypes.INCOME && regionIds.length)
        || (contractType === ContractTypes.OUTCOME && !regionIds.length)),
      suspense: false,
    },
  });

  useEffect(() => {
    if (modalState.type !== null && JSON.stringify(regionIdsRef.current) !== JSON.stringify(regionIds)) {
      regionIdsRef.current = regionIds;
      refetch();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [regionIds, modalState.type]);

  const contractors = useMemo(
    () => contractorsData?.contractors?.map(({ name, id }) => ({ label: name, value: id })) ?? [],
    [contractorsData]);

  const regionOptions: LabeledValue[] = useGetListRegions({
    suspense: false,
    enabled: modalState.type !== null,
  }).data?.map(({ name, id }) => ({ label: name, value: id })) ?? [];

  const organizationOptions: LabeledValue[] = useMemo(
    () => organizations?.map(({ officialName, id }) => ({ label: officialName, value: id })) ?? [],
    [organizations]
  );

  const editable = isCreation ?? true;

  const restrictionOptions = useMemo(() => [
    {
      value: ContractRestrictionTypes.NONE,
      label: formatContractText(contractType)`Включить всех ${{ INCOME: 'исполнителей', OUTCOME: 'клиентов' }}`,
    },
    {
      value: ContractRestrictionTypes.BLACK_LIST,
      label: formatContractText(contractType)`Исключить ${{ INCOME: 'исполнителей', OUTCOME: 'клиентов' }}`,
    },
  ], [contractType]);

  const incomeFieldsArr = useMemo<ModelFormFieldProps[]>(
    () => [
      {
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        name: 'serviceType',
        initialValue: serviceTypesDefaultValuePassengers.name,
        label: t.Contracts.serviceType,
        options: serviceTypes,
        editable,
        required: true,
        isNewDesign: true,
        disabled: true,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        name: 'transportType',
        label: t.CustomerContracts.transportType,
        options: transportTypes,
        editable: modalState.type === 'add' || modalState.type === 'filters',
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        name: 'regionIds',
        label: t.Contracts.region,
        options: regionOptions,
        showSearch: true,
        mode: 'multiple',
        optionFilterProp: 'label',
        editable,
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        name: 'organizationIds',
        label: 'Организация-клиент',
        editable: isCreation || modalState.type === 'filters',
        options: organizationOptions,
        isNewDesign: true,
        showSearch: true,
        required: true,
        placeholder: t.CustomerContracts.organization,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        name: 'contractorId',
        label: 'Агрегатор',
        options: [],
        editable: false,
        required: true,
        isNewDesign: true,
        loading: isContractorsLoading,
        placeholder: t.CustomerContracts.sTransport,
      },
      {
        fieldType: ModelFormFieldType.URL,
        onPressEnter: preventDefault,
        name: 'contractNumber',
        label: t.Contracts.contractNumber,
        editable: modalState.type === 'add',
        required: true,
        isNewDesign: true,
        rules: [
          ValidationRules.general.maxLength(30),
        ],
      },
      {
        fieldType: ModelFormFieldType.DATE_RANGE,
        allowEmpty: [false, true],
        name: 'period',
        label: t.Contracts.period,
        showTime: false,
        editable,
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.NUMBER,
        onPressEnter: preventDefault,
        name: 'sum',
        label: t.Contracts.priceWithoutVAT,
        editable: true,
        required: true,
        min: MIN_TEXT_INPUT,
        max: MAX_TEXT_INPUT,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        name: 'vatValue',
        label: 'НДС',
        options: vatValueOptions,
        editable: true,
        required: true,
        isNewDesign: true,
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [
      t,
      transportTypes,
      serviceTypes,
      editable,
      regionOptions,
      contractType,
      isContractorsLoading,
    ]
  );

  const outcomeFieldsArr = useMemo<ModelFormFieldProps[]>(
    () => [
      {
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        name: 'serviceType',
        initialValue: serviceTypesDefaultValuePassengers.name,
        label: t.Contracts.serviceType,
        options: serviceTypes,
        editable,
        required: true,
        isNewDesign: true,
        disabled: true,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        name: 'transportType',
        label: t.CustomerContracts.transportType,
        options: transportTypes,
        editable: modalState.type === 'add' || modalState.type === 'filters',
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        name: 'regionIds',
        label: t.Contracts.region,
        options: regionOptions,
        showSearch: true,
        mode: 'multiple',
        optionFilterProp: 'label',
        editable,
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        name: 'organizationIds',
        label: 'Агрегатор',
        editable: false,
        options: organizationOptions,
        isNewDesign: true,
        showSearch: true,
        required: true,
        placeholder: t.CustomerContracts.sTransport,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        name: modalState.type === 'edit' ? 'contractorName' : 'contractorId',
        label: 'Исполнитель',
        options: contractors,
        editable: isCreation || modalState.type === 'filters',
        required: true,
        isNewDesign: true,
        loading: isContractorsLoading,
        placeholder: t.CustomerContracts.contractor,
      },
      {
        fieldType: ModelFormFieldType.URL,
        onPressEnter: preventDefault,
        name: 'contractNumber',
        label: t.Contracts.contractNumber,
        editable: modalState.type === 'add',
        required: true,
        isNewDesign: true,
        rules: [
          ValidationRules.general.maxLength(30),
        ],
      },
      {
        fieldType: ModelFormFieldType.URL,
        onPressEnter: preventDefault,
        name: 'uvhd',
        label: t.CustomerContracts.uvhd,
        editable: true,
        isNewDesign: true,
        rules: [
          ValidationRules.general.onlyDigits(),
          ValidationRules.general.maxLength(12),
        ],
      },
      {
        fieldType: ModelFormFieldType.NUMBER,
        onPressEnter: preventDefault,
        name: 'sum',
        label: t.Contracts.priceWithoutVAT,
        editable: true,
        required: true,
        min: MIN_TEXT_INPUT,
        max: MAX_TEXT_INPUT,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        name: 'vatValue',
        label: 'НДС',
        options: vatValueOptions,
        editable: true,
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.DATE_RANGE,
        allowEmpty: [false, true],
        name: 'period',
        label: t.Contracts.period,
        showTime: false,
        editable,
        required: true,
        isNewDesign: true,
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [
      t,
      transportTypes,
      contractors,
      serviceTypes,
      editable,
      regionOptions,
      contractType,
      isContractorsLoading,
      regionIds,
    ]
  );

  // TODO: ЛОГИКА СО СТАРОГО ДИЗАЙНА, ОТРЕФАКТОРИТЬ
  const incomeFields = useMemo(
    () => {
      return incomeFieldsArr.map(field => {
        if (field.name === 'transportType' && field.fieldType === ModelFormFieldType.SELECT) {
          const labeledValues = typeof field.options === 'function' ? field.options() : field.options;
          return {
            ...field,
            options: labeledValues.filter(
              option => !['DEDICATED', 'COURIER', 'DOMESTIC_COURIER', 'INTERREGIONAL', 'INDIVIDUAL'].includes(option.value as string)
            ),
          };
        }
        if (field.name === 'uvhd' && contractType === ContractTypes.INCOME) {
          return { ...field, hide: true };
        }
        return field;
      });
    },
    [incomeFieldsArr, contractType]
  );

  // TODO: ЛОГИКА СО СТАРОГО ДИЗАЙНА, ОТРЕФАКТОРИТЬ
  const outcomeFields = useMemo(
    () => {
      return outcomeFieldsArr.map(field => {
        if (field.name === 'transportType' && field.fieldType === ModelFormFieldType.SELECT) {
          const labeledValues = typeof field.options === 'function' ? field.options() : field.options;
          return {
            ...field,
            options: labeledValues.filter(
              option => !['DEDICATED', 'COURIER', 'DOMESTIC_COURIER', 'INTERREGIONAL', 'INDIVIDUAL'].includes(option.value as string)
            ),
          };
        }
        if (field.name === 'uvhd' && contractType === ContractTypes.INCOME) {
          return { ...field, hide: true };
        }
        return field;
      });
    },
    [outcomeFieldsArr, contractType]
  );

  const restrictionType = useMemo<ModelFormFieldProps>(() => ({
    fieldType: ModelFormFieldType.RADIO_GROUP,
    name: 'restrictionType',
    options: restrictionOptions,
    isNewDesign: true,
    required: true,
    editable: true,
  }), [restrictionOptions]);

  const restrictedIds = useMemo<ModelFormFieldProps>(() => ({
    fieldType: ModelFormFieldType.SELECT,
    name: 'restrictedIds',
    label: formatContractText(contractType)`${{ INCOME: 'Исполнитель', OUTCOME: 'Клиент' }}`,
    options: contractType === ContractTypes.INCOME ? contractors : organizationOptions,
    mode: 'multiple',
    isNewDesign: true,
    loading: contractType === ContractTypes.INCOME ? isContractorsLoading : false,
    disabled: contractType === ContractTypes.INCOME ? !regionIds.length : false,
    required: true,
    editable: true,
  }), [contractType, contractors, organizationOptions, regionIds, isContractorsLoading]);

  return {
    incomeFields,
    outcomeFields,
    restrictionType,
    restrictedIds,
  };
};
