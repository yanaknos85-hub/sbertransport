import React, { useMemo, useState } from 'react';
import { useTranslation } from 'i18n';
import { Form, FormInstance } from 'antd';
import {
  DepartmentLevels,
  initialDepartments
} from 'constants/constants.app';
import { useProfile } from 'api/profile';
import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import { FormSectionProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { SelectDepartment } from 'shared/components/SelectDepartment/SelectDepartment';
import { ValidationRules } from 'shared/fieldValidationRules';
import { useOrganizationFiltersSearch } from './useOrganizationFiltersSearch';
import { useCargoTransportTypes, useCargoTripStatuses } from 'api/cargo-registry-search';
import { useContractors } from 'api/contractors';
import { useOrganizationProjection } from 'api/organizations/search';

import styles from 'modules/BusinessReports/components/Filters/Filters.module.scss';

export const useFilterFields: (
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onClearButtonActivator?: (event: any) => void,
  onClearDepartments?: () => void,
  form?: FormInstance<unknown>,
) => FormSectionProps[] = onClearButtonActivator => {
  const { t } = useTranslation();

  const {
    organizationId,
  } = useProfile().data;
  const [optionsDepartments, setOptionsDepartments] = useState<DepartmentLevels>(initialDepartments);
  const [organizationsValue, setOrganizationsValue] = useState<string[]>();
  const isInternalUser = true;
  const transportTypes = useCargoTransportTypes();
  const contractors = useContractors();
  const statuses = useCargoTripStatuses();

  const organizationForBusinessReports = useOrganizationProjection({}, { suspense: false }).data;

  const { organizations, handleOnSearch } = useOrganizationFiltersSearch();
  const isAllOrganizations = organizationsValue?.includes('all');

  const withAllOrganizations = useMemo(() => {
    const businessReportOrganizations = organizationForBusinessReports?.map(org => ({
      label: org.officialName,
      value: org.id.toString(),
    })) || [];

    return [
      { label: 'Все организации', value: 'all' },
      ...businessReportOrganizations,
      ...organizations,
    ];
  }, [organizationForBusinessReports, organizations, setOrganizationsValue]);

  const disabledSelectDepartment = !organizationsValue || (organizationsValue
    && (organizationsValue.length < 1 || organizationsValue.length > 1 || !organizationsValue)) || isAllOrganizations;

  return useMemo(
    (): FormSectionProps[] => [
      {
        title: 'Заявки',
        filters: [
          {
            fieldType: ModelFormFieldType.TEXT,
            name: 'requestHumanId',
            label: t.Forms.registryCargoFilters.requestIdVisible,
            editable: true,
            isNewDesign: true,
            allowClear: true,
            placeholder: 'Введите номер заявки',
            className: styles.textField,
            rules: [ValidationRules.general.minMaxLength(3, 20)],
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'cargoTransportType',
            label: t.Forms.registryCargoFilters.transportType,
            showArrow: true,
            editable: true,
            isNewDesign: true,
            allowClear: true,
            className: styles.selectField,
            options: transportTypes.data.map(item => ({ label: item.rusName, value: item.name })),
            mode: 'multiple',
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'contractorSet',
            label: t.Forms.registryCargoFilters.contractorSet,
            showArrow: true,
            editable: true,
            isNewDesign: true,
            allowClear: true,
            className: styles.selectField,
            options: contractors.data.contractors.map(item => ({ label: item.name, value: item.id })),
            mode: 'multiple',
          },
          {
            fieldType: ModelFormFieldType.TEXT,
            name: 'authorFIO',
            label: t.Forms.registryCargoFilters.authorFIO,
            editable: true,
            isNewDesign: true,
            allowClear: true,
            placeholder: 'Введите ФИО создателя',
            className: styles.textField,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'requestStatusSet',
            label: t.Forms.registryCargoFilters.tripStatus,
            showArrow: true,
            editable: true,
            isNewDesign: true,
            allowClear: true,
            className: styles.selectField,
            options: statuses.data.map(item => ({ label: item.rusName, value: item.name })),
            mode: 'multiple',
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'deadlineDate',
            label: t.Forms.registryCargoFilters.deadlineDate,
            showArrow: true,
            editable: true,
            allowClear: true,
            className: styles.selectField,
            options: [
              { label: 'Нарушен', value: 'true' },
              { label: 'Не нарушен', value: 'false' },
            ],
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'desiredDate',
            component: () => (
              <Form.Item
                name="desiredDate"
                label={t.Forms.registryCargoFilters.desiredDate}
                className={styles.dateField}
              >
                <NewDateInput
                  quarterDisabled
                  withAvailableFuture
                  placeholder="Выберите дату или диапазон"
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
            editable: true,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'creationDate',
            component: () => (
              <Form.Item
                name="creationDate"
                label={t.Forms.registryCargoFilters.creationDate}
                className={styles.dateField}
              >
                <NewDateInput
                  quarterDisabled
                  withAvailableFuture
                  placeholder="Выберите дату или диапазон"
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
            editable: true,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'changeDate',
            component: () => (
              <Form.Item
                name="changeDate"
                label={t.Forms.registryCargoFilters.changeDate}
                className={styles.dateField}
              >
                <NewDateInput
                  quarterDisabled
                  withAvailableFuture
                  placeholder="Выберите дату или диапазон"
                  onChange={onClearButtonActivator}
                />
              </Form.Item>
            ),
            editable: true,
          },
        ],
      },
      {
        title: 'Структура, данные',
        classNames: {
          sectionFields: styles.dataStructureSectionFields,
        },
        filters: [
          {
            fieldType: ModelFormFieldType.SELECT,
            editable: true,
            name: 'organizationSet',
            label: t.Forms.registryFilterFields.organization,
            placeholder: t.global.selectOrganization,
            allowClear: true,
            options: withAllOrganizations,
            className: styles.selectField,
            onSearch: handleOnSearch,
            showArrow: true,
            isNewDesign: true,
            mode: 'multiple',
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department1',
            editable: true,
            component: () => (
              <Form.Item
                name="department1"
                label={t.Forms.registryFilterFields.department1}
              >
                <SelectDepartment
                  showArrow={true}
                  isNewDesign
                  placeholder={t.Forms.registryFilterFields.department}
                  mode="multiple"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                  orgId={organizationId}
                  numberDepartment={1}
                  optionsDepartments={optionsDepartments}
                  setOptionsDepartments={setOptionsDepartments}
                  organizationsValue={organizationsValue}
                  disabled={disabledSelectDepartment}
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
                  placeholder={t.Forms.registryFilterFields.department}
                  mode="multiple"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                  orgId={organizationId}
                  numberDepartment={2}
                  optionsDepartments={optionsDepartments}
                  setOptionsDepartments={setOptionsDepartments}
                  organizationsValue={organizationsValue}
                  disabled={disabledSelectDepartment}
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
                  placeholder={t.Forms.registryFilterFields.department}
                  mode="multiple"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                  orgId={organizationId}
                  numberDepartment={3}
                  optionsDepartments={optionsDepartments}
                  setOptionsDepartments={setOptionsDepartments}
                  organizationsValue={organizationsValue}
                  disabled={disabledSelectDepartment}
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
                  placeholder={t.Forms.registryFilterFields.department}
                  mode="multiple"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                  orgId={organizationId}
                  numberDepartment={4}
                  optionsDepartments={optionsDepartments}
                  setOptionsDepartments={setOptionsDepartments}
                  organizationsValue={organizationsValue}
                  disabled={disabledSelectDepartment}
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department5',
            editable: true,
            component: () => (
              <Form.Item
                name="department5"
                label={t.Forms.registryFilterFields.department5}
              >
                <SelectDepartment
                  showArrow={true}
                  isNewDesign
                  placeholder={t.Forms.registryFilterFields.department}
                  mode="multiple"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                  orgId={organizationId}
                  numberDepartment={5}
                  optionsDepartments={optionsDepartments}
                  setOptionsDepartments={setOptionsDepartments}
                  organizationsValue={organizationsValue}
                  disabled={disabledSelectDepartment}
                />
              </Form.Item>
            ),
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department6',
            editable: true,
            component: () => (
              <Form.Item
                name="department6"
                label={t.Forms.registryFilterFields.department6}
              >
                <SelectDepartment
                  showArrow={true}
                  isNewDesign
                  placeholder={t.Forms.registryFilterFields.department}
                  mode="multiple"
                  className={styles.selectField}
                  onChange={onClearButtonActivator}
                  orgId={organizationId}
                  numberDepartment={6}
                  optionsDepartments={optionsDepartments}
                  setOptionsDepartments={setOptionsDepartments}
                  organizationsValue={organizationsValue}
                  disabled={disabledSelectDepartment}
                />
              </Form.Item>
            ),
          },
        ],
      },
    ],
    [t, isInternalUser, onClearButtonActivator]
  );
};
