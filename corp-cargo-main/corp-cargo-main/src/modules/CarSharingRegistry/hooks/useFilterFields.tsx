import React, { useMemo } from 'react';
import { useTranslation } from 'i18n';
import { Form } from 'antd';

import { useCarsharingStatuses } from 'api/engineer';
import { FormSectionProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { ValidationRules } from 'shared/fieldValidationRules';
import { NumericRange } from 'shared/components/NumericRange/NumericRange';
import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import { LabeledValue } from 'utils';

import { SelectTariff } from 'shared/components/SelectTariff/SelectTariff';
import { TransportTypesPassenger } from 'shared/components/AnalyticsFilter/constants/constants.analytics';
import { ratingOptions } from 'modules/Registry/constants/filters';
import styles from 'modules/Registry/components/Filters/Filters.module.scss';

// eslint-disable-next-line @stylistic/max-len, @typescript-eslint/no-explicit-any
export const useFilterFields: (onClearButtonActivator?: (event: any) => void) => FormSectionProps[] = onClearButtonActivator => {
  const { t } = useTranslation();
  const tripStatus = useCarsharingStatuses().data;

  const tripStatusOption: LabeledValue[] = tripStatus.map(el => ({ label: el.rusName, value: el.name }));
  const passengerOptions: LabeledValue[] = [1, 2, 3, 4].map(el => ({ label: el, value: el }));

  return useMemo(
    (): FormSectionProps[] => [
      {
        title: 'Заявка',
        filters: [
          {
            fieldType: ModelFormFieldType.TEXT,
            name: 'requestHumanId',
            label: t.Forms.registryFilterFields.requestIdVisible,
            editable: true,
            rules: [ValidationRules.general.minMaxLength(3, 20)],
            allowClear: true,
            placeholder: t.Forms.registryFilterFields.requestIdVisible,
            className: styles.textField,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'creationDate',
            component: () => (
              <Form.Item
                name="creationDate"
                label={t.Forms.registryFilterFields.creationDate}
                className={styles.dateField}
              >
                <NewDateInput
                  quarterDisabled
                  withAvailableFuture
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
            editable: true,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'desiredDate',
            component: () => (
              <Form.Item
                name="desiredDate"
                label={t.Forms.registryFilterFields.desiredDate}
                className={styles.dateField}
              >
                <NewDateInput
                  quarterDisabled
                  withAvailableFuture
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
            editable: true,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'requestStatusSet',
            label: t.Forms.registryFilterFields.tripStatus,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: tripStatusOption,
            mode: 'multiple',
            placeholder: t.Forms.registryFilterFields.tripStatus,
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'expectedCost',
            component: ({ form }) => (
              <NumericRange
                form={form}
                name="expectedCost"
                label="expectedPlanCost"
                onClearButtonActivator={onClearButtonActivator}
                className={styles.numericRangeField}
              />
            ),
            editable: true,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'passengerCountSet',
            label: t.Forms.registryFilterFields.passengerCountSet,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: passengerOptions,
            mode: 'multiple',
            placeholder: t.Forms.registryFilterFields.passengerCountSet,
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'contractorSet',
            label: t.Forms.registryFilterFields.contractorSet,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            disabled: true,
            options: [],
            mode: 'multiple',
            placeholder: t.Forms.registryFilterFields.contractorCompany,
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'tariffIdSet',
            editable: true,
            component: () => (
              <Form.Item
                name="tariffIdSet"
                className={styles.formItem}
                label={t.Forms.registryFilterFields.tariff}
              >
                <SelectTariff
                  transportType={TransportTypesPassenger.CARSHARING}
                  isNewDesign
                  placeholder="TF-****-****"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'drivingLength',
            component: ({ form }) => (
              <NumericRange
                form={form}
                name="drivingLength"
                className={styles.numericRangeField}
                onClearButtonActivator={onClearButtonActivator}
              />
            ),
            editable: true,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'ratingMarkSet',
            label: t.Forms.registryFilterFields.ratingMarkSet,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: ratingOptions,
            mode: 'multiple',
            placeholder: t.Forms.registryFilterFields.ratingMarkSet,
            className: styles.selectField,
          },
        ],
      },
      {
        title: 'Заявитель',
        filters: [
          {
            fieldType: ModelFormFieldType.TEXT,
            name: 'costCenter',
            label: t.Forms.registryFilterFields.costCenter,
            editable: true,
            allowClear: true,
            rules: [ValidationRules.general.minMaxLength(3, 50)],
            placeholder: t.Forms.registryFilterFields.costCenter,
            className: styles.textField,
          },
          {
            fieldType: ModelFormFieldType.TEXT,
            name: 'employeeFIO',
            label: t.Forms.registryFilterFields.employeeFIO,
            editable: true,
            allowClear: true,
            rules: [ValidationRules.general.minMaxLength(3, 50)],
            placeholder: t.Forms.registryFilterFields.employeeFIO,
            className: styles.textField,
          },
          {
            fieldType: ModelFormFieldType.TEXT,
            name: 'personnelNumber',
            label: t.Forms.registryFilterFields.personnelNumber,
            editable: true,
            allowClear: true,
            rules: [ValidationRules.general.minMaxLength(3, 20)],
            placeholder: t.Forms.registryFilterFields.personnelNumber,
            className: styles.textField,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM_NUMBER,
            name: 'departmentCode',
            label: t.Forms.registryFilterFields.employeeDepartment,
            editable: true,
            rules: [ValidationRules.general.validationFloatingNumbers],
            placeholder: t.DepartmentLevels.department,
            className: styles.numberField,
          },
        ],
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, tripStatusOption, onClearButtonActivator]
  );
};
