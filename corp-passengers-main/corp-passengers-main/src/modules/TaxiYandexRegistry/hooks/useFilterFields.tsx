import React, { useMemo } from 'react';
import { Form } from 'antd';
import { useTranslation } from 'i18n';

import { useProfile } from 'api/profile';
import { useGetDepartmentLevel } from 'api/departments';
import { FormSectionProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { ValidationRules } from 'shared/fieldValidationRules';
import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import type { LabeledValue } from '../types/types';
import {
  YandexTaxiRequestStatus,
  YandexTaxiRequestStatusTitles
} from 'api/yandexTaxiRegistry/yandex-taxi-registry.constants';
import { balanceUnitOptions } from 'modules/Registry/constants/filters';

import styles from 'modules/Registry/components/Filters/Filters.module.scss';

export const useFilterFields: (
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onClearButtonActivator?: (event: any) => void
) => FormSectionProps[] = onClearButtonActivator => {
  const { t } = useTranslation();
  const { organizationId } = useProfile().data;
  const requestStatusOptions: LabeledValue[] = Object.keys(YandexTaxiRequestStatus)
    .map(key => ({ label: YandexTaxiRequestStatusTitles[key], value: key }));

  const departments = useGetDepartmentLevel(organizationId).data;

  const optionsDepartments = useMemo(() => {
    const result = {
      1: [],
      2: [],
      3: [],
      4: [],
      5: [],
      6: [],
    };

    departments.map(department => {
      result[department.level].push({
        value: department.id,
        label: department.name,
      });
    });

    return result;
  }, [departments]);

  return useMemo(
    (): FormSectionProps[] => [
      {
        title: 'Заявка',
        filters: [
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'status',
            label: t.Forms.registryFilterFields.applicationStatus,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: requestStatusOptions,
            mode: 'multiple',
            placeholder: t.Forms.registryFilterFields.applicationStatus,
            className: styles.selectField,
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
                  withAvialableFuture
                  placeholder="Выберите даты поездок"
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
            editable: true,
          },
          {
            fieldType: ModelFormFieldType.TEXT,
            name: 'approverFullName',
            label: t.Forms.registryFilterFields.approvedByFioVisible,
            editable: true,
            allowClear: true,
            rules: [ValidationRules.general.minMaxLength(3, 50)],
            placeholder: t.Forms.registryFilterFields.approvedByFioVisible,
            className: styles.textField,
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
                  quarterDisabled
                  withAvialableFuture
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
            fieldType: ModelFormFieldType.TEXT,
            name: 'passengerFullName',
            label: t.Forms.registryFilterFields.passengerFio,
            editable: true,
            allowClear: true,
            rules: [ValidationRules.general.minMaxLength(3, 50)],
            placeholder: t.Forms.registryFilterFields.passengerFio,
            className: styles.textField,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'balanceUnits',
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
        ],
      },
      {
        title: '',
        sectionLine: true,
        filters: [
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'department1',
            label: t.Forms.registryFilterFields.department1,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            isAvailable: true,
            options: optionsDepartments[1],
            allowClear: true,
            placeholder: t.Forms.registryFilterFields.department1,
            className: styles.selectField,
            mode: 'multiple',
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'department2',
            label: t.Forms.registryFilterFields.department2,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            isAvailable: true,
            options: optionsDepartments[2],
            allowClear: true,
            placeholder: t.Forms.registryFilterFields.department2,
            className: styles.selectField,
            mode: 'multiple',
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'department3',
            label: t.Forms.registryFilterFields.department3,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            isAvailable: true,
            options: optionsDepartments[3],
            allowClear: true,
            placeholder: t.Forms.registryFilterFields.department3,
            className: styles.selectField,
            mode: 'multiple',
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'department4',
            label: t.Forms.registryFilterFields.department4,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            isAvailable: true,
            options: optionsDepartments[4],
            allowClear: true,
            placeholder: t.Forms.registryFilterFields.department4,
            className: styles.selectField,
            mode: 'multiple',
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'department5',
            label: t.Forms.registryFilterFields.department5,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            isAvailable: true,
            options: optionsDepartments[5],
            allowClear: true,
            placeholder: t.Forms.registryFilterFields.department5,
            className: styles.selectField,
            mode: 'multiple',
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'department6',
            label: t.Forms.registryFilterFields.department6,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            isAvailable: true,
            options: optionsDepartments[6],
            allowClear: true,
            placeholder: t.Forms.registryFilterFields.department6,
            className: styles.selectField,
            mode: 'multiple',
          },
        ],
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, optionsDepartments]
  );
};
