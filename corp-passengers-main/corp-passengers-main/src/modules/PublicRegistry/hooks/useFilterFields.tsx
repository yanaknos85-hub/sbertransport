import React, { useMemo, useState } from 'react';
import type { Dispatch, SetStateAction } from 'react';

import { useTranslation } from 'i18n';
import { Form } from 'antd';

import { TypePublicTransportOptions } from '../constants/PublicRegistry.constants';

import { useProfile } from 'api/profile';
import { useAllTripPurposes } from 'api/purposes';
import { usePublicTripStatuses } from 'api/public-register-search';

import { ValidationRules } from 'shared/fieldValidationRules';
import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import { SelectTariff } from 'shared/components/SelectTariff/SelectTariff';
import { FormSectionProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { useSelectablePublicCompensations } from 'shared/hooks/useSelectablePublicCompensations';
import { SelectDepartment } from 'shared/components/SelectDepartment/SelectDepartment';
import { DepartmentLevels } from '../types/types';
import { convertPurposesToOptions } from 'utils/purposesOptions';
import { balanceUnitOptions, paymentPeriodOptions, ratingOptions } from 'modules/Registry/constants/filters';

import styles from 'modules/Registry/components/Filters/Filters.module.scss';

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
  const {
    organizationId,
  } = useProfile().data;
  // @ts-ignore
  const { purposes } = useAllTripPurposes(organizationId).data;
  const tripStatus = usePublicTripStatuses().data;
  const purposesOptions = convertPurposesToOptions(purposes);
  const requestStatusOptions = tripStatus.map(el => ({ label: el.rusName, value: el.name }));
  const { compensationOptions } = useSelectablePublicCompensations();
  const [optionsDepartments, setOptionsDepartments] = useState<DepartmentLevels>({
    department1: [],
    department2: [],
    department3: [],
    department4: [],
    department5: [],
    department6: [],
    departmentLevel: 0,
  });
  const [compensationTypeValue, setCompensationTypeValue] = useState();

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
                  hashTariffs={hashTariffs}
                  setHashTariffs={setHashTariffs}
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
            placeholder: t.Forms.registryFilterFields.compensationType,
            className: styles.selectField,
            onChange: setCompensationTypeValue,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'publicTransportType',
            label: t.Forms.registryFilterFields.typePublicTransport,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            disabled: !compensationTypeValue,
            options: compensationTypeValue ? TypePublicTransportOptions[compensationTypeValue] : [],
            mode: 'multiple',
            placeholder: 'Выберите вид',
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
                  withAvialableFuture
                  placeholder="Выберите дату создания заявки"
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
    [t, requestStatusOptions, compensationOptions, purposesOptions, isInternalUser, onClearButtonActivator]
  );
};
