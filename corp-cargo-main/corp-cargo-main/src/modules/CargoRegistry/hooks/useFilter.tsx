import React, {
  Dispatch, SetStateAction, useCallback, useMemo, useState
} from 'react';
import { FormInstance } from 'antd/lib/form/Form';
import { useTranslation } from 'i18n';
import { useCargoTransportTypes, useCargoTripStatuses } from 'api/cargo-registry-search';
import { useContractors } from 'api/contractors';
import { clearButtonActive, transformToRangeObject } from 'utils/reportsUtils';
import { FormSectionProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { ValidationRules } from 'shared/fieldValidationRules';
import { Form } from 'antd';
import { toDateRangeISO } from 'shared/components/DateInputWithAvailableFuture/utils';
import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import { FilterValues, TransformedFilterValues } from '../types';
import { RequestType } from "shared/constants/forms.constants";

import styles from 'modules/Registry/components/Filters/Filters.module.scss';

interface UseFilterResult {
  setFilterValues: Dispatch<SetStateAction<FilterValues | undefined>>;
  isStatusChangeActive: boolean;
  setIsStatusChangeActive: Dispatch<SetStateAction<boolean>>;
  setForm: Dispatch<SetStateAction<FormInstance<any> | undefined>>;
  onClearButtonActivator: () => void;
  fields: FormSectionProps[];
  filterValues?: TransformedFilterValues;
  form?: FormInstance<any>;
}

export const useFilter = (): UseFilterResult => {
  const [filterValues, setFilterValues] = useState<FilterValues | undefined>(undefined);
  const [isStatusChangeActive, setIsStatusChangeActive] = useState(false);
  const [form, setForm] = useState<FormInstance<any>>();

  const onClearButtonActivator = useCallback(() => {
    const values = form?.getFieldsValue(true);
    setIsStatusChangeActive(clearButtonActive(values));
  }, [form, setIsStatusChangeActive]);

  const { t } = useTranslation();
  const transportTypes = useCargoTransportTypes();

  const contractors = useContractors();

  const statuses = useCargoTripStatuses();

  const fields = useMemo(
    (): FormSectionProps[] => [
      {
        filters: [
          {
            fieldType: ModelFormFieldType.TEXT,
            name: 'requestHumanId',
            label: t.Forms.registryCargoSettings.requestIdVisible,
            editable: true,
            isNewDesign: true,
            allowClear: true,
            className: styles.textField,
            placeholder: t.Forms.registryCargoSettings.requestIdVisible,
            rules: [ValidationRules.general.minMaxLength(3, 20)],
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'cargoTransportType',
            label: t.Forms.registryCargoFilters.transportType,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: transportTypes.data.map(item => ({ label: item.rusName, value: item.name })),
            mode: 'multiple',
            placeholder: t.Forms.registryCargoFilters.transportType,
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'contractorSet',
            label: t.Forms.registryCargoFilters.contractorSet,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: contractors.data.contractors.map(item => ({ label: item.name, value: item.id })),
            mode: 'multiple',
            placeholder: t.Forms.registryCargoFilters.contractorSet,
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.TEXT,
            name: 'authorFIO',
            label: t.Forms.registryCargoFilters.authorFIO,
            editable: true,
            isNewDesign: true,
            allowClear: true,
            className: styles.textField,
            placeholder: t.Forms.registryCargoFilters.authorFIO,
            rules: [ValidationRules.general.minMaxLength(3, 50)],
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'requestStatusSet',
            label: t.Forms.registryCargoFilters.tripStatus,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: statuses.data.map(item => ({ label: item.rusName, value: item.name })),
            mode: 'multiple',
            placeholder: t.Forms.registryCargoFilters.tripStatus,
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'deadlineDate',
            label: t.Forms.registryCargoFilters.deadlineDate,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: [
              { label: 'Нарушен', value: 'true' },
              { label: 'Не нарушен', value: 'false' },
            ],
            placeholder: t.Forms.registryCargoFilters.deadlineDate,
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'requestTypeSet',
            label: t.Forms.registryCargoFilters.requestTypeSet,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            mode: 'multiple',
            options: [
              { label: 'Обычная заявка', value: RequestType.SINGLE },
              { label: 'Регулярная заявка', value: RequestType.REGULAR },
              { label: 'Переезд', value: RequestType.RELOCATION },
            ],
            placeholder: t.Forms.registryCargoFilters.requestTypeSet,
            className: styles.selectField,
          },
        ],
      },
      {
        title: '',
        sectionLine: true,
        filters: [
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'desiredDate',
            editable: true,
            component: () => (
              <Form.Item
                name="desiredDate"
                label={t.Forms.registryCargoFilters.desiredDate}
                className={styles.dateField}
              >
                <NewDateInput
                  withAvailableFuture
                  onChange={onClearButtonActivator}
                  placeholder={t.Forms.registryCargoFilters.desiredDate}
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'creationDate',
            editable: true,
            component: () => (
              <Form.Item
                name="creationDate"
                label={t.Forms.registryFilterFields.creationDate}
                className={styles.dateField}
              >
                <NewDateInput
                  withAvailableFuture
                  onChange={onClearButtonActivator}
                  placeholder={t.Forms.registryFilterFields.creationDate}
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'changeDate',
            editable: true,
            component: () => (
              <Form.Item
                name="changeDate"
                label={t.Forms.registryCargoFilters.changeDate}
                className={styles.dateField}
              >
                <NewDateInput
                  withAvailableFuture
                  onChange={onClearButtonActivator}
                  placeholder={t.Forms.registryCargoFilters.changeDate}
                />
              </Form.Item>
            ),
          },
        ],
      },
    ],
    [t, onClearButtonActivator, transportTypes.data, contractors.data, statuses.data]
  );

  const transformedFilterValues = useMemo(() => {
    if (!filterValues) {
      return undefined;
    }
    let expectedCost = filterValues?.expectedCost ? transformToRangeObject(filterValues.expectedCost) : undefined;
    if (expectedCost) {
      expectedCost = {
        start: expectedCost.start * 100,
        end: expectedCost.end * 100,
      };
    }
    return {
      ...filterValues,
      desiredDate: filterValues?.desiredDate ? toDateRangeISO(filterValues.desiredDate as any) : undefined,
      changeDate: filterValues?.changeDate ? toDateRangeISO(filterValues.changeDate as any) : undefined,
      creationDate: filterValues?.creationDate ? toDateRangeISO(filterValues.creationDate as any) : undefined,
      expectedCost,
    };
  }, [filterValues]);

  return {
    setFilterValues,
    isStatusChangeActive,
    setIsStatusChangeActive,
    setForm,
    onClearButtonActivator,
    fields,
    filterValues: transformedFilterValues,
    form,
  };
};
