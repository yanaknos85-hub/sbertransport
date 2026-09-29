import React, { useMemo, useState } from 'react';
import type { Dispatch, SetStateAction } from 'react';
import { useTranslation } from 'i18n';
import { Form } from 'antd';

import { useGettingAllTravelStatuses } from 'api/travel-status';
import { useProfile } from 'api/profile';
import { coopTrip } from 'stores/RegistryTaxi/Registry.interface';

import { LabeledValue } from 'utils/Types';
import { NumericRange } from 'shared/components/NumericRange/NumericRange';
import { SelectTariff } from 'shared/components/SelectTariff/SelectTariff';
import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import { FormSectionProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { ValidationRules } from 'shared/fieldValidationRules';
import { SelectDepartment } from 'shared/components/SelectDepartment/SelectDepartment';

import styles from 'modules/Registry/components/Filters/Filters.module.scss';
import { balanceUnitOptions, paymentPeriodOptions, ratingOptions } from 'modules/Registry/constants/filters';
import { DepartmentLevels } from '../types/types';

enum TransportTypesPassenger {
  TAXI = 'TAXI',
  PERSONAL = 'PERSONAL',
  PUBLIC = 'PUBLIC',
  CARSHARING = 'CARSHARING',
}

export const useFilterFields: (
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onClearButtonActivator: (event: any) => void,
  hashTariffs: Record<string, string>,
  setHashTariffs: Dispatch<SetStateAction<Record<string, string>>>
) => FormSectionProps[] = (
  onClearButtonActivator,
  hashTariffs,
  setHashTariffs
) => {
  const { t } = useTranslation();
  const tripStatuses = useGettingAllTravelStatuses().data.filter(t => t.name.includes('PERSONAL'));
  const tripStatusOption: LabeledValue[] = tripStatuses.map(el => ({ label: el.rusName, value: el.name }));
  const coopTripOptions: LabeledValue[] = coopTrip.map(el => ({ label: el.name, value: el.isCoop }));
  const passengerOptions: LabeledValue[] = [1, 2, 3, 4, 5].map(el => ({ label: el, value: el }));
  const [optionsDepartments, setOptionsDepartments] = useState<DepartmentLevels>({
    department1: [],
    department2: [],
    department3: [],
    department4: [],
    department5: [],
    department6: [],
    departmentLevel: 0,
  });
  const {
    organizationId,
  } = useProfile().data;

  return useMemo(
    (): FormSectionProps[] => [
      {
        title: 'Заявка',
        filters: [
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
            fieldType: ModelFormFieldType.SELECT,
            name: 'paymentPeriod',
            label: t.Forms.registryFilterFields.paymentPeriod,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: paymentPeriodOptions,
            placeholder: t.Forms.registryFilterFields.paymentPeriod,
            className: styles.selectField,
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
            fieldType: ModelFormFieldType.BOOLEAN,
            name: 'passenger',
            label: t.Forms.registryFilterFields.passenger,
            editable: true,
            allowClear: true,
            placeholder: t.Positions.SelfApprovedPlaceholder,
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
                  transportType={TransportTypesPassenger.PERSONAL}
                  isNewDesign
                  placeholder="TF-****-****"
                  mode="multiple"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                  hashTariffs={hashTariffs}
                  setHashTariffs={setHashTariffs}
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'expectedDistance',
            component: ({ form }) => (
              <NumericRange
                form={form}
                name="expectedDistance"
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
                  withAvialableFuture
                  quarterDisabled
                  placeholder="Выберите дату создания заявки"
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
            editable: true,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'orderPaymentFormationStartRange',
            component: () => (
              <Form.Item
                name="orderPaymentFormationStartRange"
                label={t.Forms.registryFilterFields.paymentOrderDate}
                className={styles.dateField}
              >
                <NewDateInput
                  withAvialableFuture
                  quarterDisabled
                  placeholder="Выберите дату формирования приказа"
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
            fieldType: ModelFormFieldType.SELECT,
            name: 'balanceUnitSet',
            label: t.Forms.registryFilterFields.balanceUnit,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            isAvailable: true,
            options: balanceUnitOptions,
            mode: 'multiple',
            allowClear: true,
            placeholder: t.Forms.registryFilterFields.balanceUnit,
            className: styles.selectField,
          },
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
            className: styles.numberField,
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
      {
        title: '',
        sectionLine: true,
        filters: [
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department1',
            editable: true,
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
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department2',
            editable: true,
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
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department3',
            editable: true,
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
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department4',
            editable: true,
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
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department5',
            editable: true,
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
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department6',
            editable: true,
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
                />
              </Form.Item>
            ),
          },
        ],
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, coopTripOptions, tripStatusOption]
  );
};
