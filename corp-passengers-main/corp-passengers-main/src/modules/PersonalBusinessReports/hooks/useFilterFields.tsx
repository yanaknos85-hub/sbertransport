import React, { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'i18n';
import { Form, type FormInstance } from 'antd';
import moment from 'moment';

import { SelectDepartment } from 'shared/components/SelectDepartment/SelectDepartment';
import { initialDepartments } from 'constants/constants.app';

import { useGettingAllTravelStatuses } from 'api/travel-status';
import { coopTrip } from 'stores/RegistryTaxi/Registry.interface';

import { LabeledValue } from 'utils/Types';
import { NewDateInput } from 'shared/components/DatePicker/DatePicker';
import { FormSectionProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { paymentPeriodOptions, ratingOptions } from 'modules/Registry/constants/filters';
import { useAllTripPurposes } from 'api/purposes';
import { useOrganizationProjection } from 'api/organizations/search';
import { convertPurposesToOptions } from 'utils/purposesOptions';
import { DepartmentLevels } from 'modules/PersonalRegistry/types/types';
import { useProfile } from 'api/profile';
import { useAllStatusCancellationCodes } from 'api/statusCancellationCodes';
import { TransportTypes } from 'modules/TripSettings/constants/tripSettings';
import { deadlineStateOptions, savingsOptions } from 'modules/BusinessReports/constants/filters';
import { ValidationRules } from 'shared/fieldValidationRules';
import { useOrganizationFiltersSearch } from 'modules/TaxiBusinessReports/hooks/useOrganizationFiltersSearch';

import styles from '../components/Filters/Filters.module.scss';

export const useFilterFields: (
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onClearButtonActivator?: (event: any) => void,
  onClearDepartments?: () => void,
  form?: FormInstance<unknown>,
) => FormSectionProps[] = (onClearButtonActivator, onClearDepartments, form) => {
  const { t } = useTranslation();
  const tripStatuses = useGettingAllTravelStatuses().data.filter(t => t.name.includes('PERSONAL'));
  const tripStatusOption: LabeledValue[] = tripStatuses.map(el => ({ label: el.rusName, value: el.name }));
  const coopTripOptions: LabeledValue[] = coopTrip.map(el => ({ label: el.name, value: el.isCoop }));
  const passengerOptions: LabeledValue[] = [1, 2, 3, 4, 5].map(el => ({ label: el, value: el }));
  const [optionsDepartments, setOptionsDepartments] = useState<DepartmentLevels>(initialDepartments);
  const [organizationsValue, setOrganizationsValue] = useState<string[]>();
  const {
    organizationId,
  } = useProfile().data;
  const { purposes } = useAllTripPurposes(organizationId).data;
  const purposesOptions = convertPurposesToOptions(purposes);
  const [statusCodes, setStatusCodes] = useState<LabeledValue[]>([]);
  const [statusCancellationCodes] = useAllStatusCancellationCodes(TransportTypes.PERSONAL);

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
                  withAvialableFuture
                  quarterDisabled
                  placeholder="Выберите дату или диапозон"
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
                rules={[ValidationRules.general.required, ValidationRules.general.validationDateCompletion()]}
              >
                <NewDateInput
                  withAvialableFuture
                  quarterDisabled
                  placeholder="Выберите дату или диапозон"
                  onChange={onClearButtonActivator}
                  disabledDateRange={disabledDate}
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
            label: t.Forms.registryFilterFields.applicationStatus,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: tripStatusOption,
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
            name: 'coopTrip',
            label: t.Forms.registryFilterFields.typeTrip,
            labelInValue: true,
            editable: true,
            showArrow: true,
            isNewDesign: true,
            allowClear: true,
            options: coopTripOptions,
            placeholder: 'Выберите тип',
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
    [t, coopTripOptions, tripStatusOption]
  );
};
