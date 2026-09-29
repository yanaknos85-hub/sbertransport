import React, {
  Dispatch, SetStateAction, useCallback, useMemo, useState
} from 'react';
import { Form } from 'antd';
import { FormInstance } from 'antd/lib/form/Form';
import { useTranslation } from 'i18n';
import { useContractors } from 'api/contractors';
import { useCargoTransportTypes, useCargoTripStatuses } from 'api/cargo-registry-search';
import { clearButtonActive } from 'utils/reportsUtils';
import { FormSectionProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { ValidationRules } from 'shared/fieldValidationRules';
import { toDateRangeISO } from 'shared/components/DateInputWithAvailableFuture/utils';
import { SelectDepartment } from 'shared/components/SelectDepartment/SelectDepartment';
import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import { useProfile } from 'api/profile';
import { Value } from 'modules/OrderExecution/components/Filter/Inputs/DatePickerRange/types';
import { DepartmentLevels, FilterValues } from '../types';
import { statusOptions } from '../constants';

import styles from 'modules/Registry/components/Filters/Filters.module.scss';

interface UseFilterResult {
  setFilterValues: Dispatch<SetStateAction<FilterValues>>;
  isStatusChangeActive: boolean;
  setIsStatusChangeActive: Dispatch<SetStateAction<boolean>>;
  setForm: Dispatch<SetStateAction<FormInstance | undefined>>;
  onClearButtonActivator: () => void;
  fields: FormSectionProps[];
  filterValues?: FilterValues;
  form?: FormInstance;
}

export const useFilter = (): UseFilterResult => {
  const [filterValues, setFilterValues] = useState<FilterValues>({} as FilterValues);
  const [isStatusChangeActive, setIsStatusChangeActive] = useState(false);
  const [form, setForm] = useState<FormInstance>();

  const [optionsDepartments, setOptionsDepartments] = useState<DepartmentLevels>({
    department1: [],
    department2: [],
    department3: [],
    department4: [],
    department5: [],
    department6: [],
    departmentLevel: 0,
  });

  const onClearButtonActivator = useCallback(() => {
    const values = form?.getFieldsValue(true);
    setIsStatusChangeActive(clearButtonActive(values));
  }, [form, setIsStatusChangeActive]);

  const { t } = useTranslation();
  const transportTypes = useCargoTransportTypes();
  const contractors = useContractors();
  const statuses = useCargoTripStatuses();

  const { organizationId } = useProfile().data || {};

  const fields = useMemo(
    (): FormSectionProps[] => [
      {
        filters: [
          {
            fieldType: ModelFormFieldType.TEXT,
            name: 'humanReadableId',
            label: t.Forms.registryCargoCompensationsFilters.humanReadableId,
            editable: true,
            isNewDesign: true,
            allowClear: true,
            className: styles.textField,
            placeholder: t.Forms.registryCargoCompensationsFilters.humanReadableId,
            rules: [ValidationRules.general.minMaxLength(3, 20)],
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'statusSet',
            label: t.Forms.registryCargoCompensationsFilters.statusSet,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: statusOptions,
            mode: 'multiple',
            placeholder: t.Forms.registryCargoCompensationsFilters.statusSet,
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'personnelNumber',
            label: t.Forms.registryCargoCompensationsFilters.personnelNumber,
            editable: true,
            isNewDesign: true,
            allowClear: true,
            mode: 'tags',
            allowCustomInput: true,
            options: [],
            className: styles.selectField,
            placeholder: t.Forms.registryCargoCompensationsFilters.personnelNumberPh,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'approvalDateRange',
            component: () => (
              <Form.Item
                name="approvalDateRange"
                label={t.Forms.registryCargoCompensationsFilters.approvalDateRange}
                className={styles.dateField}
              >
                <NewDateInput
                  withAvailableFuture
                  placeholder={t.Forms.registryCargoCompensationsFilters.approvalDate}
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
            editable: true,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'formationDateRange',
            component: () => (
              <Form.Item
                name="formationDateRange"
                label={t.Forms.registryCargoCompensationsFilters.formationDateRange}
                className={styles.dateField}
              >
                <NewDateInput
                  withAvailableFuture
                  placeholder={t.Forms.registryCargoCompensationsFilters.formationDate}
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
            editable: true,
          },
        ],
      },
      {
        title: '',
        sectionLine: true,
        filters: [
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department1',
            editable: true,
            isNewDesign: true,
            component: () => (
              <Form.Item name="department1" label={t.Forms.registryFilterFields.department1}>
                <SelectDepartment
                  showArrow={true}
                  isNewDesign
                  placeholder={t.Forms.registryFilterFields.department1}
                  mode="multiple"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                  orgId={organizationId}
                  numberDepartment={1}
                  optionsDepartments={optionsDepartments}
                  setOptionsDepartments={setOptionsDepartments}
                  singleOrganization={true}
                  disabled
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department2',
            editable: true,
            isNewDesign: true,
            component: () => (
              <Form.Item name="department2" label={t.Forms.registryFilterFields.department2}>
                <SelectDepartment
                  showArrow={true}
                  isNewDesign
                  placeholder={t.Forms.registryFilterFields.department2}
                  mode="multiple"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                  orgId={organizationId}
                  numberDepartment={2}
                  optionsDepartments={optionsDepartments}
                  setOptionsDepartments={setOptionsDepartments}
                  singleOrganization={true}
                  disabled
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department3',
            editable: true,
            isNewDesign: true,
            component: () => (
              <Form.Item name="department3" label={t.Forms.registryFilterFields.department3}>
                <SelectDepartment
                  showArrow={true}
                  isNewDesign
                  placeholder={t.Forms.registryFilterFields.department3}
                  mode="multiple"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                  orgId={organizationId}
                  numberDepartment={3}
                  optionsDepartments={optionsDepartments}
                  setOptionsDepartments={setOptionsDepartments}
                  singleOrganization={true}
                  disabled
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department4',
            editable: true,
            isNewDesign: true,
            component: () => (
              <Form.Item name="department4" label={t.Forms.registryFilterFields.department4}>
                <SelectDepartment
                  showArrow={true}
                  isNewDesign
                  placeholder={t.Forms.registryFilterFields.department4}
                  mode="multiple"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                  orgId={organizationId}
                  numberDepartment={4}
                  optionsDepartments={optionsDepartments}
                  setOptionsDepartments={setOptionsDepartments}
                  singleOrganization={true}
                  disabled
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department5',
            editable: true,
            isNewDesign: true,
            component: () => (
              <Form.Item name="department5" label={t.Forms.registryFilterFields.department5}>
                <SelectDepartment
                  showArrow={true}
                  isNewDesign
                  placeholder={t.Forms.registryFilterFields.department5}
                  mode="multiple"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                  orgId={organizationId}
                  numberDepartment={5}
                  optionsDepartments={optionsDepartments}
                  setOptionsDepartments={setOptionsDepartments}
                  singleOrganization={true}
                  disabled
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department6',
            editable: true,
            isNewDesign: true,
            component: () => (
              <Form.Item name="department6" label={t.Forms.registryFilterFields.department6}>
                <SelectDepartment
                  showArrow={true}
                  isNewDesign
                  placeholder={t.Forms.registryFilterFields.department6}
                  mode="multiple"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                  orgId={organizationId}
                  numberDepartment={6}
                  optionsDepartments={optionsDepartments}
                  setOptionsDepartments={setOptionsDepartments}
                  singleOrganization={true}
                  disabled
                />
              </Form.Item>
            ),
          },
        ],
      },
    ],
    [
      t,
      onClearButtonActivator,
      transportTypes.data,
      contractors.data,
      statuses.data,
      organizationId,
      optionsDepartments,
    ]
  );

  const transformedFilterValues = useMemo((): FilterValues | undefined => {
    if (!filterValues) {
      return undefined;
    }
    const approvalDateRange = filterValues.approvalDateRange
      ? toDateRangeISO(filterValues.approvalDateRange as unknown as Value)
      : undefined;
    const formationDateRange = filterValues.formationDateRange
      ? toDateRangeISO(filterValues.formationDateRange as unknown as Value)
      : undefined;

    return {
      ...filterValues,
      approvalDateRange,
      formationDateRange,
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
