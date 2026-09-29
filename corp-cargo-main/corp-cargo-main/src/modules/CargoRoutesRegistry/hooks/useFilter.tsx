import React, {
  Dispatch, SetStateAction, useCallback, useMemo, useState
} from 'react';
import { Form } from 'antd';
import { LabeledValue } from 'antd/lib/select';
import { FormInstance } from 'antd/lib/form/Form';
import { useTranslation } from 'i18n';
import { useContractors } from 'api/contractors';
import { useGetListRegions } from 'api/tariffs-cargo';
import { useCargoTransportTypes, useCargoTripStatuses } from 'api/cargo-registry-search';
import { clearButtonActive } from 'utils/reportsUtils';
import { FormSectionProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { ValidationRules } from 'shared/fieldValidationRules';
import { toDateRangeISO } from 'shared/components/DateInputWithAvailableFuture/utils';
import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import {
  FilterValues, Statuses, StatusNames, TransformedFilterValues
} from '../types';

import styles from 'modules/Registry/components/Filters/Filters.module.scss';

interface UseFilterResult {
  setFilterValues: Dispatch<SetStateAction<FilterValues>>;
  isStatusChangeActive: boolean;
  setIsStatusChangeActive: Dispatch<SetStateAction<boolean>>;
  setForm: Dispatch<SetStateAction<FormInstance | undefined>>;
  onClearButtonActivator: () => void;
  fields: FormSectionProps[];
  filterValues?: TransformedFilterValues;
  form?: FormInstance;
}

export const useFilter = (): UseFilterResult => {
  const [filterValues, setFilterValues] = useState<FilterValues>({} as FilterValues);
  const [isStatusChangeActive, setIsStatusChangeActive] = useState(false);
  const [form, setForm] = useState<FormInstance>();

  const onClearButtonActivator = useCallback(() => {
    const values = form?.getFieldsValue(true);
    setIsStatusChangeActive(clearButtonActive(values));
  }, [form, setIsStatusChangeActive]);

  const { t } = useTranslation();
  const transportTypes = useCargoTransportTypes();

  const contractors = useContractors();
  const contractorsOptions = contractors.data.contractors.map(item => ({ label: item.name, value: item.id }));

  const statuses = useCargoTripStatuses();
  const geoZonesList = useGetListRegions().data;
  const geoZonesOptions: LabeledValue[] = geoZonesList.reduce<LabeledValue[]>((acc, { name }) => {
    if (!acc.some(option => option.label === name)) {
      acc.push({
        label: name,
        value: name,
      });
    }
    return acc;
  }, []);

  const statusOptions = [
    { label: StatusNames[Statuses.CARGO_PLANNING], value: Statuses.CARGO_PLANNING },
    { label: StatusNames[Statuses.CARGO_PLANNING_FINISHED], value: Statuses.CARGO_PLANNING_FINISHED },
    { label: StatusNames[Statuses.CARGO_AWAITING_DATA], value: Statuses.CARGO_AWAITING_DATA },
    { label: StatusNames[Statuses.CARGO_AWAITING_TRANSFER], value: Statuses.CARGO_AWAITING_TRANSFER },
    { label: StatusNames[Statuses.CARGO_TRANSFER_FINISHED], value: Statuses.CARGO_TRANSFER_FINISHED },
    { label: StatusNames[Statuses.CARGO_SHIPMENT_FINISHED], value: Statuses.CARGO_SHIPMENT_FINISHED },
    { label: StatusNames[Statuses.CARGO_CANCELED], value: Statuses.CARGO_CANCELED },
  ];

  const fields = useMemo(
    (): FormSectionProps[] => [
      {
        filters: [
          {
            fieldType: ModelFormFieldType.TEXT,
            name: 'humanReadableId',
            label: t.Forms.registryCargoRoutesFilters.humanReadableId,
            editable: true,
            isNewDesign: true,
            className: styles.textField,
            placeholder: t.Forms.registryCargoRoutesFilters.humanReadableId,
            rules: [ValidationRules.general.minMaxLength(3, 20)],
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'regionFrom',
            label: t.Forms.registryCargoRoutesFilters.regionFrom,
            editable: true,
            isNewDesign: true,
            allowClear: true,
            showArrow: true,
            options: geoZonesOptions,
            mode: 'multiple',
            className: styles.selectField,
            placeholder: t.Forms.registryCargoRoutesFilters.regionFrom,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'regionTo',
            label: t.Forms.registryCargoRoutesFilters.regionTo,
            editable: true,
            isNewDesign: true,
            allowClear: true,
            showArrow: true,
            options: geoZonesOptions,
            mode: 'multiple',
            className: styles.selectField,
            placeholder: t.Forms.registryCargoRoutesFilters.regionTo,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'creationDateRange',
            editable: true,
            isNewDesign: true,
            component: () => (
              <Form.Item
                name="creationDateRange"
                label={t.Forms.registryCargoRoutesFilters.creationDateRange}
                className={styles.dateField}
              >
                <NewDateInput
                  withAvailableFuture
                  placeholder={t.Forms.registryCargoRoutesFilters.creationDate}
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'desiredDateRange',
            editable: true,
            isNewDesign: true,
            component: () => (
              <Form.Item
                name="desiredDateRange"
                label={t.Forms.registryCargoRoutesFilters.desiredDateRange}
                className={styles.dateField}
              >
                <NewDateInput
                  withAvailableFuture
                  placeholder={t.Forms.registryCargoRoutesFilters.desiredDate}
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
          },
        ],
      },
      {
        title: '',
        sectionLine: true,
        filters: [
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'statusSet',
            label: t.Forms.registryCargoRoutesFilters.statusSet,
            editable: true,
            isNewDesign: true,
            allowClear: true,
            showArrow: true,
            options: statusOptions,
            mode: 'multiple',
            className: styles.selectField,
            placeholder: t.Forms.registryCargoRoutesFilters.statusSet,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'contractorSet',
            label: t.Forms.registryCargoRoutesFilters.contractorSet,
            editable: true,
            isNewDesign: true,
            allowClear: true,
            showArrow: true,
            options: contractorsOptions,
            mode: 'multiple',
            className: styles.selectField,
            placeholder: t.Forms.registryCargoRoutesFilters.contractorSet,
          },
        ],
      },
    ],
    [t, onClearButtonActivator, transportTypes.data, contractors.data, statuses.data, geoZonesOptions]
  );
  const transformedFilterValues = useMemo(() => {
    if (!filterValues) {
      return undefined;
    }
    return {
      ...filterValues,
      desiredDateRange: filterValues?.desiredDateRange
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
        ? toDateRangeISO(filterValues.desiredDateRange as any) : undefined,
      creationDateRange: filterValues?.creationDateRange
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
        ? toDateRangeISO(filterValues.creationDateRange as any) : undefined,
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
