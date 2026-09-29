import React, { useMemo } from 'react';

import { useTranslation } from 'i18n';
import { Form } from 'antd';
import { LabeledValue } from 'antd/lib/select';

import { useProfile } from 'api/profile';
import { useAllTripPurposes } from 'api/purposes';
import { usePublicTripStatuses, useSearchUserByDepartments } from 'api/public-register-search';

import { ValidationRules } from 'shared/fieldValidationRules';
import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import { SelectTariff } from 'shared/components/SelectTariff/SelectTariff';
import { FormSectionProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { useSelectablePublicCompensations } from 'shared/hooks/useSelectablePublicCompensations';

import { convertPurposesToOptions } from 'utils/purposesOptions';
import { balanceUnitOptions, paymentPeriodOptions, ratingOptions } from 'modules/Registry/constants/filters';
import styles from 'modules/Registry/components/Filters/Filters.module.scss';
import { OrgStructureType } from 'constants/constants.app';

enum TransportTypesPassenger {
  TAXI = 'TAXI',
  PERSONAL = 'PERSONAL',
  PUBLIC = 'PUBLIC',
  CARSHARING = 'CARSHARING',
}

export const useFilterFields: (onClearButtonActivator?: (event: any) => void) => FormSectionProps[] = onClearButtonActivator => {
  const { t } = useTranslation();
  const {
    organizationId, departmentId, id,
  } = useProfile().data;
  // @ts-ignore
  const { purposes } = useAllTripPurposes(organizationId).data;
  const tripStatus = usePublicTripStatuses().data;
  const purposesOptions = convertPurposesToOptions(purposes);
  const requestStatusOptions = tripStatus.map(el => ({ label: el.rusName, value: el.name }));
  const { compensationOptions } = useSelectablePublicCompensations();

  // В рамках задачи TRANSPORT-18762 решено пока отображать все поля вне зависмости от признака!
  // Возможно, в будущем потребуется доработка!
  // const departmentUser = useSearchUserByDepartments({ organizationId, departmentId, id }).data;
  // const isInternalUser = departmentUser?.orgStructureType === OrgStructureType.INTERNAL;

  const isInternalUser = true;

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
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'tariffIdSet',
            editable: true,
            component: () => (
              <Form.Item name="tariffIdSet" label={t.Forms.registryFilterFields.tariff}>
                <SelectTariff
                  showArrow={true}
                  transportType={TransportTypesPassenger.PUBLIC}
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
            fieldType: ModelFormFieldType.SELECT,
            name: 'requestStatusSet',
            label: t.Forms.registryFilterFields.requestStatus,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: requestStatusOptions,
            mode: 'multiple',
            placeholder: t.Forms.registryFilterFields.requestStatus,
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
            fieldType: ModelFormFieldType.SELECT,
            name: 'compensationType',
            label: t.Forms.registryFilterFields.compensationType,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: compensationOptions,
            mode: 'multiple',
            placeholder: t.Forms.registryFilterFields.compensationType,
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'purposeSet',
            label: t.Forms.registryFilterFields.purposeTrip,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: purposesOptions,
            mode: 'multiple',
            placeholder: t.Forms.registryFilterFields.purposeTrip,
            className: styles.selectField,
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
            name: 'orderPaymentFormationStartDate',
            component: () => (
              <Form.Item
                name="orderPaymentFormationStartDate"
                label={t.Forms.registryFilterFields.paymentOrderDate}
                className={styles.dateField}
              >
                <NewDateInput
                  quarterDisabled
                  withAvailableFuture
                  placeholder="Выберете дату формирования приказа"
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
            isAvailable: isInternalUser,
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
            isAvailable: isInternalUser,
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
    ],
    [t, requestStatusOptions, compensationOptions, purposesOptions, isInternalUser, onClearButtonActivator]
  );
};
