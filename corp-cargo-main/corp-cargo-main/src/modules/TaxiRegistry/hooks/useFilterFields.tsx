import React, { useMemo } from 'react';
import { Form } from 'antd';
import { useTranslation } from 'i18n';

import { useContractors } from 'api/contractors';
import { useGetTaxiTripStatus } from 'api/register-search';

import { coopTrip } from 'stores/RegistryTaxi/Registry.interface';
import { FormSectionProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { ValidationRules } from 'shared/fieldValidationRules';
import { NumericRange } from 'shared/components/NumericRange/NumericRange';
import { SelectTariff } from 'shared/components/SelectTariff/SelectTariff';
import { TransportTypesPassenger } from 'shared/components/AnalyticsFilter/constants/constants.analytics';
import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import { LabeledValue } from 'utils';
import { ratingOptions } from 'modules/Registry/constants/filters';
import styles from 'modules/Registry/components/Filters/Filters.module.scss';

export const useFilterFields: (onClearButtonActivator?: (event: any) => void) => FormSectionProps[] = onClearButtonActivator => {
  const { t } = useTranslation();
  const { contractors } = useContractors().data;

  const tripStatus = useGetTaxiTripStatus().data;
  const coopTripOptions: LabeledValue[] = coopTrip.map(el => ({ label: el.name, value: el.isCoop }));
  const tripStatusOption: LabeledValue[] = tripStatus.map(el => ({ label: el.rusName, value: el.name }));
  const contractorOptions: LabeledValue[] = contractors.map(el => ({ label: el.name, value: el.id }));
  const passengerOptions: LabeledValue[] = [1, 2, 3, 4, 5].map(el => ({ label: el, value: el }));

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
            allowClear: true,
            rules: [ValidationRules.general.minMaxLength(3, 20)],
            placeholder: 'OT-****-********',
            className: styles.textField,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'coopTrip',
            label: t.Forms.registryFilterFields.typeTrip,
            labelInValue: true,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: coopTripOptions,
            placeholder: t.Forms.registryFilterFields.typeTrip,
            className: styles.selectField,
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
                label="preliminaryCost"
                onClearButtonActivator={onClearButtonActivator}
                className={styles.numericRangeField}
              />
            ),
            editable: true,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'economyPercent',
            component: ({ form }) => (
              <NumericRange
                disabled
                form={form}
                name="economyPercent"
                className={styles.numericRangeField}
                onClearButtonActivator={onClearButtonActivator}
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
            options: contractorOptions,
            mode: 'multiple',
            placeholder: t.Forms.registryFilterFields.contractorSet,
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'tariffIdSet',
            editable: true,
            component: () => (
              <Form.Item name="tariffIdSet" label={t.Forms.registryFilterFields.tariff}>
                <SelectTariff
                  showArrow={true}
                  transportType={TransportTypesPassenger.TAXI}
                  isNewDesign
                  placeholder="TF-****-****"
                  mode="multiple"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'factDistance',
            component: ({ form }) => (
              <NumericRange
                form={form}
                name="factDistance"
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
                  placeholder="Выберете дату создания заявки"
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
            editable: true,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'desiredDateRange',
            component: () => (
              <Form.Item
                name="desiredDateRange"
                label={t.Forms.registryFilterFields.desiredDateRange}
                className={styles.dateField}
              >
                <NewDateInput
                  quarterDisabled
                  withAvailableFuture
                  placeholder="Выберете желаемую дату поездки"
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
            editable: true,
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
            label: t.Forms.registryFilterFields.passengerFio,
            editable: true,
            allowClear: true,
            rules: [ValidationRules.general.minMaxLength(3, 50)],
            placeholder: t.Forms.registryFilterFields.passengerFio,
            className: styles.textField,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM_NUMBER,
            name: 'personnelNumber',
            label: t.Forms.registryFilterFields.personnelNumber,
            editable: true,
            rules: [ValidationRules.general.minMaxLength(5, 20)],
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
            className: styles.textField,
          },
          {
            fieldType: ModelFormFieldType.TEXT,
            name: 'sharedRideOwnerFIO',
            label: t.Forms.registryFilterFields.sharedRideOwner,
            editable: true,
            allowClear: true,
            rules: [ValidationRules.general.minMaxLength(3, 50)],
            placeholder: t.Forms.registryFilterFields.sharedRideOwner,
            className: styles.textField,
          },
        ],
      },
    ],
    [t, coopTripOptions, tripStatusOption, contractorOptions]
  );
};
