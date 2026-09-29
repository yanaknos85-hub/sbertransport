import React, { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'i18n';
import { Form, FormInstance } from 'antd';
import moment from 'moment';

import {
  AllTransportTypes,
  DepartmentLevels,
  deadlineStateOptions,
  initialDepartments,
  paymentPeriodOptions,
  publicCompensationDocumentExistOptions,
  ratingOptions,
  savingsOptions
} from 'constants/constants.app';
import { TypePublicTransportOptions } from '../constants/PublicBusinessReports.constants';

import { useProfile } from 'api/profile';
import { useAllTripPurposes } from 'api/purposes';
import { usePublicTripStatuses } from 'api/public-register-search';
import { useAllStatusCancellationCodes } from 'api/statusCancellationCodes';
import { useOrganizationProjection } from 'api/organizations/search';

import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import { FormSectionProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { useSelectablePublicCompensations } from 'shared/hooks/useSelectablePublicCompensations';
import { SelectDepartment } from 'shared/components/SelectDepartment/SelectDepartment';
import { convertPurposesToOptions } from 'utils/purposesOptions';
import { LabeledValue } from 'utils/Types';
import { ValidationRules } from 'shared/fieldValidationRules';
import { useOrganizationFiltersSearch } from 'modules/TaxiBusinessReports/hooks/useOrganizationFiltersSearch';

import styles from 'modules/BusinessReports/components/Filters/Filters.module.scss';

export const useFilterFields: (
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onClearButtonActivator?: (event: any) => void,
  onClearDepartments?: () => void,
  form?: FormInstance<unknown>,
) => FormSectionProps[] = (onClearButtonActivator, onClearDepartments, form) => {
  const { t } = useTranslation();
  const {
    organizationId,
  } = useProfile().data;
  // @ts-ignore
  const { purposes } = useAllTripPurposes(organizationId).data;
  const [statusCancellationCodes] = useAllStatusCancellationCodes(AllTransportTypes.PUBLIC);
  const tripStatus = usePublicTripStatuses().data;
  const purposesOptions = convertPurposesToOptions(purposes);
  const requestStatusOptions = tripStatus.map(el => ({ label: el.rusName, value: el.name }));
  const { compensationOptions } = useSelectablePublicCompensations();
  const [statusCodes, setStatusCodes] = useState<LabeledValue[]>([]);
  const [optionsDepartments, setOptionsDepartments] = useState<DepartmentLevels>(initialDepartments);
  const [organizationsValue, setOrganizationsValue] = useState<string[]>();
  const [typePublicTransportOptions, setTypePublicTransportOptions] = useState<LabeledValue[]>([]);
  const [compensationTypeValue, setCompensationTypeValue] = useState();

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

  // В рамках задачи TRANSPORT-18762 решено пока отображать все поля вне зависмости от признака!
  // Возможно, в будущем потребуется доработка!
  // const departmentUser = useSearchUserByDepartments({ organizationId, departmentId, id }).data;
  // const isInternalUser = departmentUser?.orgStructureType === OrgStructureType.INTERNAL;

  const isInternalUser = true;

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

  useEffect(() => {
    setCompensationTypeValue(form?.getFieldValue('compensationType'));
  }, [form?.getFieldValue('compensationType')]);

  useEffect(() => {
    compensationTypeValue && setTypePublicTransportOptions(TypePublicTransportOptions[compensationTypeValue]);
  }, [compensationTypeValue]);

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
            name: 'orderPaymentFormationStartDate',
            component: () => (
              <Form.Item
                name="orderPaymentFormationStartDate"
                label={t.Forms.registryFilterFields.paymentOrderDate}
                className={styles.dateField}
                rules={[ValidationRules.general.required, ValidationRules.general.validationDateCompletion()]}
              >
                <NewDateInput
                  quarterDisabled
                  withAvialableFuture
                  disabledDateRange={disabledDate}
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
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: purposesOptions,
            mode: 'multiple',
            placeholder: 'Выберите цели',
            className: styles.selectField,
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
            name: 'paymentPeriod',
            label: t.Forms.registryFilterFields.paymentPeriod,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: paymentPeriodOptions,
            placeholder: 'Выберите тип',
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
            name: 'compensationType',
            label: t.Forms.registryFilterFields.ticketType,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: compensationOptions,
            placeholder: 'Выберите тип',
            className: styles.selectField,
            onChange: value => {
              form?.resetFields(['publicTransportType']);
              setCompensationTypeValue(value);
            },
          },
          {
            fieldType: ModelFormFieldType.SELECT,
            name: 'publicTransportType',
            label: t.Forms.registryFilterFields.typePublicTransport,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: typePublicTransportOptions,
            mode: 'multiple',
            placeholder: 'Выберите вид',
            className: styles.selectField,
            disabled: !compensationTypeValue,
          },
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
            name: 'publicCompensationDocumentExist',
            label: t.Forms.registryFilterFields.publicCompensationDocumentExist,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: publicCompensationDocumentExistOptions,
            placeholder: 'Выберите признак',
            className: styles.selectField,
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
    [t, requestStatusOptions, compensationOptions, purposesOptions, isInternalUser, onClearButtonActivator]
  );
};
