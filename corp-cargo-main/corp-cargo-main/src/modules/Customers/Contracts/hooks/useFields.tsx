/* eslint-disable no-unused-vars, @typescript-eslint/no-unused-vars */
import React, { useMemo } from 'react';
import type { Dispatch, SetStateAction } from 'react';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'i18n';
import { useTransportTypes } from 'api/transport-types';
import { useServiceTypes } from 'api/service-types';
import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { ValidationRules } from 'shared/fieldValidationRules';
import { useContractors } from 'api/contractors';
import { useGetListRegions } from 'api/tariffs';
import { LabeledValue, preventDefault } from 'utils';
import {
  MAX_TEXT_INPUT, MIN_TEXT_INPUT, serviceTypesDefaultValueCargo
} from '../constants/constants';
import { useModal } from '../context/modal.context';
import { useOrganizationProjection } from 'api/organizations/search';
import { formatContractText } from '../utils/formatContractText';
import { ContractRestrictionTypes, ContractTypes, vatValueTitles } from 'constants/constants.app';

import styles from './styles.module.scss';

const vatValueOptions = Object.entries(vatValueTitles).map(([value, label]) => ({ value: +value, label }));

export const useFields = ({ ...rest }: { vatChecked?: boolean;
  setVatChecked?: Dispatch<SetStateAction<boolean>>;
  isCreation?: boolean;
  modalStateType?: string;
  regionIds: string[];
}) => {
  const { modalState } = useModal();
  const { t } = useTranslation();

  const {
    vatChecked, setVatChecked, isCreation, modalStateType, regionIds = [],
  } = rest;

  const organizations = useOrganizationProjection({}, { suspense: false }).data;
  const { contractType } = useParams<{ contractType: ContractTypes }>();

  // запросы для селектов
  const transportTypes = useTransportTypes({
    suspense: false,
    enabled: modalState.type !== null,
  }).data?.map(({ name, rusName }) => ({ value: name, label: rusName })) ?? [];

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
    // params: { regionIds },
    config: {
      enabled: modalState.type !== null
      && ((contractType === ContractTypes.INCOME && regionIds.length)
      || (contractType === ContractTypes.OUTCOME && !regionIds.length)),
      suspense: false,
    },
  });

  const contractors = useMemo(
    () => contractorsData?.contractors?.map(({ name, id }) => ({ label: name, value: id })) ?? [],
    [contractorsData]
  );

  const regionOptions: LabeledValue[] = useGetListRegions({
    suspense: false,
  }).data?.map(({ name, id }) => ({ label: name, value: id })) ?? [];

  const organizationOptions: LabeledValue[]
  = organizations?.map(({ officialName, id }) => ({ label: officialName, value: id })) ?? [];

  const editable = isCreation ?? true;

  const restrictionOptions = useMemo(() => [
    {
      value: ContractRestrictionTypes.NONE,
      label: formatContractText(contractType)`Включить всех ${{ INCOME: 'исполнителей', OUTCOME: 'клиентов' }}`,
    },
    {
      value: ContractRestrictionTypes.SELECT_LIST,
      label: formatContractText(contractType)`Выбрать ${{ INCOME: 'исполнителей', OUTCOME: 'клиентов' }}`,
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
        initialValue: serviceTypesDefaultValueCargo.name,
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
        label: t.Contracts.transportType,
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
        editable: true,
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        name: 'organizationIds',
        label: 'Организация-клиент',
        editable: true,
        options: organizationOptions,
        isNewDesign: true,
        mode: modalStateType === 'filters' ? undefined : 'multiple',
        showSearch: true,
        required: true,
      },
      {
        fieldType: ModelFormFieldType.CUSTOM,
        onInputKeyDown: preventDefault,
        name: 'contractorId',
        label: 'Агрегатор',
        editable: false,
        required: true,
        isNewDesign: true,
        component: () => (
          <div className={styles.container}>
            <label htmlFor="contractorId" className={styles.label}>Агрегатор</label>
            <div id="contractorId" className={styles.disabledField}>
              {t.CustomerContracts.sTransport}
            </div>
          </div>
        ),
      },
      {
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        name: 'contractNumber',
        label: t.Contracts.contractNumberDzo,
        editable: modalState.type === 'add',
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.DATE_RANGE,
        allowEmpty: [false, true],
        name: 'period',
        label: t.Contracts.period,
        showTime: false,
        editable: true,
        required: true,
        isNewDesign: true,
        disabled: !isCreation,
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
        rules: [
          ValidationRules.general.onlyDigits(),
          ValidationRules.general.greaterThanZero(),
        ],
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        name: 'vatValue',
        label: t.Contracts.vatValue,
        options: vatValueOptions,
        editable: true,
        required: true,
        isNewDesign: true,
        showSearch: false,
      },
    ],
    [t, transportTypes, contractors, serviceTypes, vatChecked, editable, regionOptions, setVatChecked]
  );

  const outcomeFieldsArr = useMemo<ModelFormFieldProps[]>(
    () => [
      {
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        name: 'serviceType',
        initialValue: serviceTypesDefaultValueCargo.name,
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
        label: t.Contracts.transportType,
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
        editable: true,
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        name: 'contractorId',
        label: t.CustomerContracts.contractor,
        options: contractors,
        editable: isCreation || modalState.type === 'filters',
        required: true,
        isNewDesign: true,
        loading: isContractorsLoading,
        placeholder: t.CustomerContracts.contractor,
      },
      {
        fieldType: ModelFormFieldType.CUSTOM,
        onInputKeyDown: preventDefault,
        name: 'contractorId',
        label: 'Агрегатор',
        editable: false,
        required: true,
        isNewDesign: true,
        component: () => (
          <div className={styles.container}>
            <label htmlFor="contractorId" className={styles.label}>Агрегатор</label>
            <div id="contractorId" className={styles.disabledField}>
              {t.CustomerContracts.sTransport}
            </div>
          </div>
        ),
      },
      {
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        name: 'contractNumber',
        label: t.Contracts.contractNumberDzo,
        editable: modalState.type === 'add',
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        name: 'uvhd',
        label: t.Contracts.externalContractNumber,
        editable: true,
        required: true,
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
        rules: [
          ValidationRules.general.onlyDigits(),
          ValidationRules.general.greaterThanZero(),
        ],
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        name: 'vatValue',
        label: t.Contracts.vatValue,
        options: vatValueOptions,
        editable: true,
        required: true,
        isNewDesign: true,
        showSearch: false,
      },
      {
        fieldType: ModelFormFieldType.DATE_RANGE,
        allowEmpty: [false, false],
        name: 'period',
        label: t.Contracts.period,
        showTime: false,
        editable: true,
        required: true,
        isNewDesign: true,
        disabled: !isCreation,
      },
    ],
    [t, transportTypes, contractors, serviceTypes, vatChecked, editable, regionOptions, setVatChecked]
  );

  const notEditType = modalState.type === 'add' || modalState.type === 'filters';

  // TODO: ЛОГИКА СО СТАРОГО ДИЗАЙНА, ОТРЕФАКТОРИТЬ
  const incomeFields = useMemo(
    () => {
      return incomeFieldsArr.map(field => {
        if (field.name === 'transportType' && field.fieldType === ModelFormFieldType.SELECT) {
          const labeledValues = typeof field.options === 'function' ? field.options() : field.options;
          return {
            ...field,
            options: labeledValues.filter(option => ['DEDICATED', 'COURIER', 'DOMESTIC_COURIER', 'INTERREGIONAL', 'INDIVIDUAL'].includes(option.value as string)
            ),
          };
        }
        return field;
      });
    },
    [incomeFieldsArr, contractType]
  );

  const outcomeFields = useMemo(
    () => {
      return outcomeFieldsArr.map(field => {
        if (field.name === 'transportType' && field.fieldType === ModelFormFieldType.SELECT) {
          const labeledValues = typeof field.options === 'function' ? field.options() : field.options;
          return {
            ...field,
            options: labeledValues.filter(option => ['DEDICATED', 'COURIER', 'DOMESTIC_COURIER', 'INTERREGIONAL', 'INDIVIDUAL'].includes(option.value as string)
            ),
          };
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
    initialValue: ContractRestrictionTypes.NONE,
    className: styles.radioGroup,
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
