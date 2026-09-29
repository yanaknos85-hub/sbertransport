import React, { useMemo, useState } from 'react';
import { Form } from 'antd';
import { useTranslation } from 'i18n';

import { useContractors } from 'api/contractors';
import { useGetListRegions } from 'api/tariffs';
import { useGroupTransferTransportClasses } from 'api/passengers-transport-classes';
import { useActiveTripPurposes } from 'api/purposes';
import { useProfile } from 'api/profile';

import { FormSectionProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { ValidationRules } from 'shared/fieldValidationRules';
import { NumericRange } from 'shared/components/NumericRange/NumericRange';
import { SelectTariff } from 'shared/components/SelectTariff/SelectTariff';
import { TransportTypesPassenger } from 'stores/RegistryTaxi/Registry.interface';
import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import { SelectDepartment } from 'shared/components/SelectDepartment/SelectDepartment';

import { LabeledValue } from 'utils';
import { balanceUnitOptions, ratingOptions } from 'modules/Registry/constants/filters';

import { GroupTransferStatusNames } from '../constants/groupTransfer.constants';

import styles from 'modules/Registry/components/Filters/Filters.module.scss';
import { DepartmentLevels } from '../types/types';

export const useFilterFields: (onClearButtonActivator?: () => void) => FormSectionProps[] = onClearButtonActivator => {
  const { t } = useTranslation();
  const { contractors } = useContractors().data;
  const { organizationId } = useProfile().data;
  const { data: groupTransferClasses } = useGroupTransferTransportClasses();
  const { data: purposesData } = useActiveTripPurposes(organizationId);
  const { data: regions } = useGetListRegions();
  const [optionsDepartments, setOptionsDepartments] = useState<DepartmentLevels>({
    department1: [],
    department2: [],
    department3: [],
    department4: [],
    department5: [],
    department6: [],
    departmentLevel: 0,
  });

  const transferClassOptions: LabeledValue[] = (
    groupTransferClasses.map(({ rusName: label, value }) => ({ label, value }))
  );
  const groupTransferStatusOption: LabeledValue[] = (
    Object.entries(GroupTransferStatusNames).map(([value, label]) => ({ label, value }))
  );
  const contractorOptions: LabeledValue[] = contractors.map(({ name: label, id: value }) => ({ label, value }));
  const purposeOptions: LabeledValue[] = purposesData.purposes.map(({ label, id: value }) => ({ label, value }));
  const regionOptions: LabeledValue[] = regions.map(({ name: label, id: value }) => ({ label, value }));

  // См. src/modules/AdminApp/PublicTransportTripRegistry/hooks/useFilterFields.tsx
  const isInternalUser = true;

  return useMemo(
    (): FormSectionProps[] => [
      {
        title: 'Заявка',
        filters: [
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'groupTransferClassList',
            label: t.Forms.registryFilterFields.serviceType,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: transferClassOptions,
            mode: 'multiple',
            placeholder: t.Forms.registryFilterFields.serviceType,
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
            options: groupTransferStatusOption,
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
            name: 'regionSet',
            label: t.Forms.registryFilterFields.region,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: regionOptions,
            mode: 'multiple',
            placeholder: t.Forms.registryFilterFields.region,
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
                  transportType={TransportTypesPassenger.GROUP_TRANSFER}
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
                  placeholder="Выберете желаемую дату поездки"
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
            editable: true,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'purposeSet',
            label: t.Forms.registryFilterFields.purposeTrip,
            labelInValue: true,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: purposeOptions,
            mode: 'multiple',
            placeholder: t.Forms.registryFilterFields.purposeTrip,
            className: styles.selectField,
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
    [t, groupTransferStatusOption, contractorOptions]
  );
};
