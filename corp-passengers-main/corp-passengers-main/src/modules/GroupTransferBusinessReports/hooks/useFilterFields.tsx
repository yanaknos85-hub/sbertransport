import React, { useEffect, useMemo, useState } from 'react';
import { Form } from 'antd';
import { useTranslation } from 'i18n';
import moment from 'moment';

import { GroupTransferStatusNames } from '../constants/groupTransfer.constants';
import {
  deadlineStateOptions, savingsOptions, DepartmentLevels, ratingOptions,
  initialDepartments
} from 'constants/constants.app';

import { useContractors } from 'api/contractors';
import { useActiveTripPurposes } from 'api/purposes';
import { useProfile } from 'api/profile';
import { useAllStatusCancellationCodes } from 'api/statusCancellationCodes';
import { useOrganizationProjection } from 'api/organizations/search';

import { SelectDepartment } from 'shared/components/SelectDepartment/SelectDepartment';
import { FormSectionProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import { LabeledValue } from 'utils';
import { ValidationRules } from 'shared/fieldValidationRules';
import { useOrganizationFiltersSearch } from 'modules/TaxiBusinessReports/hooks/useOrganizationFiltersSearch';

import styles from 'modules/BusinessReports/components/Filters/Filters.module.scss';
import { FormInstance } from 'antd/es/form';

export const useFilterFields: (
  onClearButtonActivator?: () => void,
  onClearDepartments?: () => void,
  form?: FormInstance<unknown>,
) => FormSectionProps[] = (onClearButtonActivator, onClearDepartments, form) => {
  const { t } = useTranslation();
  const { contractors } = useContractors().data;
  const { organizationId } = useProfile().data;
  const { data: purposesData } = useActiveTripPurposes(organizationId);
  const [optionsDepartments, setOptionsDepartments] = useState<DepartmentLevels>(initialDepartments);
  const [organizationsValue, setOrganizationsValue] = useState<string[]>();
  const passengerOptions: LabeledValue[] = [...Array(18)].map((_, i) => i + 1).map(el => ({ label: el, value: el }));

  const groupTransferStatusOption: LabeledValue[] = (
    Object.entries(GroupTransferStatusNames).map(([value, label]) => ({ label, value }))
  );
  const contractorOptions: LabeledValue[] = contractors.map(({ name: label, id: value }) => ({ label, value }));
  const purposeOptions: LabeledValue[] = purposesData.purposes.map(({ label, id: value }) => ({ label, value }));
  const [statusCodes, setStatusCodes] = useState<LabeledValue[]>([]);
  const [statusCancellationCodes] = useAllStatusCancellationCodes('TRANSFER');

  useEffect(() => {
    statusCancellationCodes().then(el => {
      if (el) {
        const statusCodesFinal = el.statusCodes.map(item => ({
          value: item.statusCode,
          label: item.description,
        }));

        setStatusCodes(statusCodesFinal);
      }
    });
  }, []);

  const organizationProjection = useOrganizationProjection({}, { suspense: false }).data;

  const { organizations, handleOnSearch } = useOrganizationFiltersSearch();
  const isAllOrganizations = organizationsValue?.includes('all');

  const withAllOrganizations = useMemo(() => {
    const businessReportOrganizations = organizationProjection?.map(org => ({
      label: org.officialName,
      value: org.id.toString(),
    })) || [];

    return [
      { label: 'Все организации', value: 'all' },
      ...businessReportOrganizations,
      ...organizations,
    ];
  }, [organizationProjection, organizations]);

  const disabledDate = (d: moment.Moment) => {
    const minDate = moment().subtract(5, 'year');
    const maxDate = moment().add(3, 'months').startOf('day');
    return (d && moment(d) < minDate) || moment(d) > maxDate;
  };

  const handleChangeOrganizations = e => {
    const isLastAll = e.at(-1) === 'all';
    const updatedOrganizations = isAllOrganizations
      ? e.filter(item => item !== 'all')
      : e;

    if (form) {
      form.setFieldsValue({
        employeeOrganizationSet: isLastAll ? ['all'] : updatedOrganizations,
      });
    }

    setOrganizationsValue(
      isLastAll ? ['all'] : updatedOrganizations
    );

    setOptionsDepartments(initialDepartments);
    onClearDepartments && onClearDepartments();
  };

  const disabledSelectDepartment = !organizationsValue || (organizationsValue
    && (organizationsValue.length < 1 || organizationsValue.length > 1 || !organizationsValue)) || isAllOrganizations;

  return useMemo(
    (): FormSectionProps[] => [
      {
        title: 'Заявки',
        filters: [
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
                  placeholder="Выберите дату или диапозон"
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
                rules={[ValidationRules.general.required, ValidationRules.general.validationDateCompletion()]}
              >
                <NewDateInput
                  quarterDisabled
                  withAvialableFuture
                  placeholder="Выберете желаемую дату поездки"
                  onChange={onClearButtonActivator}
                  disabledDateRange={disabledDate}
                />
              </Form.Item>
            ),
            editable: true,
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'requestClosedDatetime',
            component: () => (
              <Form.Item
                name="requestClosedDatetime"
                label={t.Forms.registryFilterFields.requestClosedDatetime}
                className={styles.dateField}
              >
                <NewDateInput
                  quarterDisabled
                  withAvialableFuture
                  placeholder="Выберите дату или диапозон"
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
            placeholder: 'Выберите цели',
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'requestStatusSet',
            label: t.Forms.registryFilterFields.applicationStatus,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: groupTransferStatusOption,
            mode: 'multiple',
            placeholder: 'Выберите статус',
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'requestStatusCodes',
            label: t.Forms.registryFilterFields.requestStatusCodes,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: statusCodes,
            mode: 'multiple',
            placeholder: 'Выберите причину',
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'deadlineState',
            label: t.Forms.registryFilterFields.deadlineState,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: deadlineStateOptions,
            placeholder: 'Выберите признак',
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'passengerCountSet',
            label: t.Forms.registryFilterFields.numberPassengersIndicatedTrip,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: passengerOptions,
            mode: 'multiple',
            placeholder: 'Выберите количество',
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
            placeholder: 'Выберите оценку',
            className: styles.selectField,
          },
        ],
      },
      {
        title: 'Исполнение',
        filters: [
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'savings',
            label: t.Forms.registryFilterFields.savings,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: savingsOptions,
            placeholder: 'Выберите признак',
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
            placeholder: 'Укажите контрагента',
            className: styles.selectField,
          },
          {
            fieldType: ModelFormFieldType.TEXT,
            name: 'contractNumber',
            label: t.Forms.registryFilterFields.contractNumber,
            editable: true,
            allowClear: true,
            placeholder: 'Укажите номер',
            className: styles.textField,
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
            name: 'employeeOrganizationSet',
            label: t.Forms.registryFilterFields.organization,
            placeholder: t.global.selectOrganization,
            allowClear: true,
            options: withAllOrganizations,
            className: styles.selectField,
            onSearch: handleOnSearch,
            showArrow: true,
            isNewDesign: true,
            onChange: handleChangeOrganizations,
            mode: 'multiple',
          },
          {
            fieldType: ModelFormFieldType.CUSTOM,
            name: 'department1',
            editable: true,
            component: () => (
              <Form.Item name="department1" label={t.Forms.registryFilterFields.department1}>
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
              <Form.Item name="department5" label={t.Forms.registryFilterFields.department5}>
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
              <Form.Item name="department6" label={t.Forms.registryFilterFields.department6}>
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, groupTransferStatusOption, contractorOptions]
  );
};
