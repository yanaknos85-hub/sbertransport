import { useMemo } from 'react';
import type { Dispatch, FormEventHandler, SetStateAction } from 'react';
import moment from 'moment';

import { useTranslation } from 'i18n';
import { useTransportTypes } from 'api/transport-types';
import { useServiceTypes } from 'api/service-types';
import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { useContractors } from 'api/contractors';
import { useGetListRegions } from 'api/tariffs';
import { LabeledValue, preventDefault } from 'utils';
import {
  MAX_TEXT_INPUT, MIN_TEXT_INPUT, serviceTypesDefaultValuePassengers
} from '../constants/constants';
import { useModal } from '../context/modal.context';
import { TariffsHanbookTitles } from '../../../../NewTariffs/constants/Tariffs.constants';
import { useOrganizationProjection } from 'api/organizations/search';
import { ValidationRules } from 'shared/fieldValidationRules';
import { AllTransportTypes, vatValueTitles } from 'constants/constants.app';

const handleInputNumber: FormEventHandler<HTMLInputElement> = e => {
  // Оставляем только цифры, точку и запятую
  e.currentTarget.value = e.currentTarget.value?.replace(/[^0-9.,]/g, '');

  // Проверяем, есть ли уже точка или запятая
  let hasSeparator = false;
  e.currentTarget.value = e.currentTarget.value
    .split('')
    .map(char => {
      if (char === '.' || char === ',') {
        if (!hasSeparator) {
          hasSeparator = true;
          return char;
        } else {
          return ''; // Игнорируем дополнительные точки/запятые
        }
      }
      return char;
    })
    .join('');
};

export const useFields = ({ ...rest }: {
  vatChecked?: boolean;
  setVatChecked?: Dispatch<SetStateAction<boolean>>;
  isCreation?: boolean;
  modalStateType?: string;
  regionIds?: string[];
  transportType?: string;
  startDate?: moment.Moment;
}) => {
  const { modalState } = useModal();
  const { t } = useTranslation();

  const {
    vatChecked,
    setVatChecked,
    isCreation,
    modalStateType,
    transportType,
    regionIds,
    startDate,
  } = rest;

  const isTransfer = transportType === AllTransportTypes.GROUP_TRANSFER;
  const organizations = useOrganizationProjection({}, { suspense: false }).data;

  const editable = isCreation ?? true;

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
  const contractors = useContractors({
    config: {
      suspense: false,
    },
  }).data?.contractors?.map(({ name, id }) => ({ label: name, value: id })) ?? [];

  const regionOptions: LabeledValue[] = useGetListRegions({
    suspense: false,
  }).data?.map(({ name, id }) => ({
    label: name,
    value: id,
    ...(isTransfer && !editable ? { disabled: (regionIds ?? []).includes(id) } : {}),
  })) ?? [];

  const organizationOptions: LabeledValue[]
    = organizations?.map(({ officialName, id }) => ({ label: officialName, value: id })) ?? [];

  const disabledPassedDate = (d: moment.Moment) => d.startOf('day').isBefore(startDate);

  const fieldsArr = useMemo<ModelFormFieldProps[]>(
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
        label: t.Contracts.transportType,
        options: transportTypes,
        editable: modalState.type === 'add' || modalState.type === 'filters',
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        name: 'organizationIds',
        label: TariffsHanbookTitles.organizationId,
        editable: isCreation || modalState.type === 'filters' || false,
        options: organizationOptions,
        isNewDesign: true,
        mode: modalStateType === 'filters' ? undefined : 'multiple',
        showSearch: true,
        required: true,
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
        editable: isTransfer || editable,
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        name: 'contractorId',
        label: t.Contracts.contractorId,
        options: contractors,
        editable: modalState.type === 'add' || modalState.type === 'filters',
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        name: 'contractNumber',
        label: t.Contracts.contractNumber,
        editable: modalState.type === 'add',
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.TEXT,
        onPressEnter: preventDefault,
        name: 'uvhd',
        label: t.Contracts.uvhd,
        editable: !isTransfer,
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
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        name: 'vatValue',
        label: t.Contracts.vatValueLabel,
        editable: true,
        options: Object.entries(vatValueTitles).map(([value, label]) => ({ value, label })),
        isNewDesign: true,
        showSearch: false,
        required: true,
        tooltip: t.Contracts.vatValueTooltip,
      },
      {
        fieldType: ModelFormFieldType.DATE_RANGE,
        allowEmpty: [false, true],
        name: 'period',
        label: t.Contracts.period,
        showTime: false,
        editable: isTransfer || editable,
        required: true,
        isNewDesign: true,
        ...(!editable && isTransfer ? {
          disabledDate: disabledPassedDate,
          disabled: [true, false] as boolean & [boolean, boolean],
        } : {}),
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, transportTypes, contractors, serviceTypes, vatChecked, editable, regionOptions, setVatChecked]
  );

  // TODO: ЛОГИКА СО СТАРОГО ДИЗАЙНА, ОТРЕФАКТОРИТЬ
  const fields = useMemo(
    () => {
      return fieldsArr.map(field => {
        if (field.name === 'transportType' && field.fieldType === ModelFormFieldType.SELECT) {
          const labeledValues = typeof field.options === 'function' ? field.options() : field.options;
          return {
            ...field,
            options: labeledValues.filter(
              option => !['DEDICATED', 'COURIER', 'DOMESTIC_COURIER', 'INTERREGIONAL', 'INDIVIDUAL'].includes(option.value as string)
            ),
          };
        }
        return field;
      });
    },
    [fieldsArr]
  );

  const penaltyFields = useMemo<ModelFormFieldProps[]>(
    () => [
      {
        fieldType: ModelFormFieldType.NUMBER,
        onPressEnter: preventDefault,
        name: 'driverLatePickupPenalty',
        label: t.Contracts.driverLatePickupPenalty,
        editable: true,
        required: true,
        min: 0,
        max: 1,
        isNewDesign: true,
        decimalSeparator: ',',
        onInputCapture: handleInputNumber,
        precision: 2,
        isUiKit: true,
      },
      {
        fieldType: ModelFormFieldType.NUMBER,
        onPressEnter: preventDefault,
        name: 'poorServiceQualityPenalty',
        label: t.Contracts.poorServiceQualityPenalty,
        editable: true,
        required: true,
        min: 0,
        max: 1,
        isNewDesign: true,
        decimalSeparator: ',',
        onInputCapture: handleInputNumber,
        precision: 2,
        isUiKit: true,
      },
      {
        fieldType: ModelFormFieldType.NUMBER,
        onPressEnter: preventDefault,
        name: 'driverOrderCancellationPenalty',
        label: t.Contracts.driverOrderCancellationPenalty,
        editable: true,
        required: true,
        min: 0,
        max: 1,
        isNewDesign: true,
        decimalSeparator: ',',
        onInputCapture: handleInputNumber,
        precision: 2,
        isUiKit: true,
      },
    ],
    [t]
  );

  return { fields, penaltyFields };
};
