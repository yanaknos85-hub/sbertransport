import React, { useMemo } from 'react';
import type { Dispatch, SetStateAction } from 'react';
import { useTranslation } from 'i18n';
import { useTransportTypes } from 'api/transport-types';
import { useServiceTypes } from 'api/service-types';
import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { ValidationRules } from 'shared/fieldValidationRules';
import { useContractors } from 'api/contractors';
import { useGetListRegions } from 'api/tariffs';
import { ignore, LabeledValue, preventDefault } from 'utils';
import {
  MAX_TEXT_INPUT, MIN_TEXT_INPUT, serviceTypesDefaultValueCargo
} from '../constants/constants';
import { vatValueTitles } from 'constants/constants.app';
import { TemplateCheckbox } from '../components/TemplateCheckbox';
import { PurposeCheckbox } from '../components/PurposeCheckbox';
import { useModal } from '../context/modal.context';
import { TariffsHanbookTitles } from '../../../../NewTariffs/constants/Tariffs.constants';
import { useOrganizationProjection } from 'api/organizations/search';
import { StyledTitleRow } from 'modules/TariffSettings/styled/styled.tariffs';

export const useFields = ({ ...rest }: {vatChecked?: boolean;
  setVatChecked?: Dispatch<SetStateAction<boolean>>;
  templateChecked: boolean;
  setTemplateChecked?: Dispatch<SetStateAction<boolean>>;
  isCreation?: boolean;
  modalStateType?: string;
  purposeChecked: boolean;
  setPurposeChecked: Dispatch<SetStateAction<boolean>>;
}) => {
  const { modalState } = useModal();
  const { t } = useTranslation();

  const {
    vatChecked, setVatChecked, templateChecked, setTemplateChecked, isCreation, modalStateType, purposeChecked, setPurposeChecked
  } = rest;

  const organizations = useOrganizationProjection({}, { suspense: false }).data;

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
  }).data?.map(({ name, id }) => ({ label: name, value: id })) ?? [];

  const organizationOptions: LabeledValue[]
  = organizations?.map(({ officialName, id }) => ({ label: officialName, value: id })) ?? [];
  const vatValueOptions = Object.entries(vatValueTitles).map(([value, label]) => ({ value: +value, label }));

  const editable = isCreation ?? true;

  const fieldsArr = useMemo<ModelFormFieldProps[]>(
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
        name: 'organizationIds',
        label: TariffsHanbookTitles.organizationId,
        editable: true,
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
        editable: true,
        required: true,
        isNewDesign: true,
      },
      {
        fieldType: ModelFormFieldType.SELECT,
        onInputKeyDown: preventDefault,
        name: 'contractorId',
        label: t.Contracts.contractorId,
        options: contractors ,
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
      {
        fieldType: ModelFormFieldType.CUSTOM,
        name: 'template',
        label: '',
        editable: true,
        required: templateChecked,
        component: () => <TemplateCheckbox 
          templateChecked={templateChecked ?? false}
          setTemplateChecked={setTemplateChecked ?? ignore} 
          initialValue={t.Contracts.template}
          disabled={modalState.type === 'edit'}
          onToggleOther={() => setPurposeChecked?.(false)} 
        />,
      },
      {
        fieldType: ModelFormFieldType.CUSTOM,
        name: 'purpose',
        label: '',
        editable: true,
        required: purposeChecked,
        component: () => <PurposeCheckbox 
          purposeChecked={purposeChecked ?? false}
          setPurposeChecked={setPurposeChecked ?? ignore} 
          initialValue={t.Contracts.purpose}
          disabled={modalState.type === 'edit'}
          onToggleOther={() => setTemplateChecked?.(false)}
        />,
      },
      {
        fieldType: ModelFormFieldType.CUSTOM,
        name: 'contractsParams',
        editable: false,
        hiddenInFilters: true,
        component: () => <StyledTitleRow>Параметры договора</StyledTitleRow>,
      },
      {
        fieldType: ModelFormFieldType.TIME,
        allowEmpty: [false, false],
        // Нужна доработка бэка!
        // Поле приведено к формату snake_case, т.к. данные приходя в этом формате.
        // Иначе будет необходимо приводить данные к camelCase и затем при отправке данных на бэк снова приводить к snake_case.
        name: 'CARGO_ROUTE_START_PLANNING_DEADLINE',
        label: t.Contracts.cargoRouteStartPlanningDeadline,
        editable: true,
        required: true,
        isNewDesign: true,
        hiddenInFilters: true,
      },
      {
        fieldType: ModelFormFieldType.TIME,
        allowEmpty: [false, false],
        // Нужна доработка бэка!
        // Поле приведено к формату snake_case, т.к. данные приходя в этом формате.
        // Иначе будет необходимо приводить данные к camelCase и затем при отправке данных на бэк снова приводить к snake_case.
        name: 'CARGO_ROUTE_BACK_PLANNING_DEADLINE',
        label: t.Contracts.cargoRouteBackPlanningDeadline,
        editable: true,
        required: true,
        isNewDesign: true,
        hiddenInFilters: true,
      },
      {
        fieldType: ModelFormFieldType.TIME,
        allowEmpty: [false, false],
        // Нужна доработка бэка!
        // Поле приведено к формату snake_case, т.к. данные приходя в этом формате.
        // Иначе будет необходимо приводить данные к camelCase и затем при отправке данных на бэк снова приводить к snake_case.
        name: 'CARGO_ROUTE_FINISH_PLANNING_DEADLINE',
        label: t.Contracts.cargoRouteFinishPlanningDeadline,
        editable: true,
        required: true,
        isNewDesign: true,
        hiddenInFilters: true,
      },
    ],
    [t, transportTypes, contractors, serviceTypes, vatChecked, editable, regionOptions, setVatChecked, templateChecked, purposeChecked]
  );

  // TODO: ЛОГИКА СО СТАРОГО ДИЗАЙНА, ОТРЕФАКТОРИТЬ
  const fields = useMemo(
    () => {
      return fieldsArr.map(field => {
        if (field.name === 'transportType' && field.fieldType === ModelFormFieldType.SELECT) {
          const labeledValues = typeof field.options === 'function' ? field.options() : field.options;
          return {
            ...field,
            options: labeledValues.filter(option =>
              ['DEDICATED', 'COURIER', 'DOMESTIC_COURIER', 'INTERREGIONAL', 'INDIVIDUAL'].includes(option.value as string),
            ),
          };
        }
        return field;
      });
    },
    [fieldsArr]
  );

  return { fields };
};
