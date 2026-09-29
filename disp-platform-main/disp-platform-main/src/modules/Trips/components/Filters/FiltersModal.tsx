import React, { FC, useEffect } from 'react';
import { Form, Modal } from 'antd';
import { useForm } from 'antd/lib/form/Form';
import { Store } from 'antd/lib/form/interface';
import moment, { Moment } from 'moment';
import { useTranslation } from 'i18n';
import { StatusSelect } from 'components/StatusSelect';
import { NewDateInput } from 'components/DatePicker/DatePicker';
import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import { TripTypes } from 'constants/app.constants';
import { ServiceType } from 'constants/trips.constants';
import { SelectDrivers } from 'components/SelectDrivers/SelectDrivers';
import { Option, Select } from 'components/Select';
import { useTripsQuery } from 'modules/Trips/context/TripsQuery';
import { useTripsModal } from '../../context/TripsModal';
import { serviceTypeOptions, serviceTypeToTaxiClasses } from './filters.constants';
import styles from './index.module.scss';

const ONE_DAY = 1000 * 60 * 60 * 24;

export const FiltersModal: FC = () => {
  const { t } = useTranslation();
  const [form] = useForm();

  const { isOpened, closeModal } = useTripsModal();

  const { query, setQuery } = useTripsQuery();

  const handleSuccess = () => {
    form.submit();
    closeModal();
  };

  const handleCancel = () => {
    closeModal();
  };

  useEffect(() => {
    if (!isOpened('filters')) return;

    const {
      desireDateStart, desireDateEnd, driverIds, serviceType, ...other
    } = query;

    form.setFieldsValue({
      ...other,
      serviceType: serviceType ?? undefined,
      startTime: {
        mode: Math.abs(moment(desireDateEnd).diff(moment(desireDateStart))) > ONE_DAY ? 'range' : 'date',
        value: [
          desireDateStart ? moment(desireDateStart) : null,
          desireDateEnd ? moment(desireDateEnd) : null,
        ],
      },
      driverIds,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query, form, isOpened('filters')]);

  const onFinish = (values: Store) => {
    const [desireDateStart, desireDateEnd] = values.startTime?.value as (Moment | null)[];
    const endDate = desireDateEnd ?? desireDateStart;

    const serviceTypeValue = values.serviceType as ServiceType | undefined;
    const taxiClass = serviceTypeValue
      ? serviceTypeToTaxiClasses[serviceTypeValue]
      : undefined;

    const filters = {
      ...values,
      statuses: values.statuses,
      desireDateStart: desireDateStart?.clone().startOf('day').utc().format() ?? undefined,
      desireDateEnd: endDate?.clone().endOf('day').utc().format() ?? undefined,
      requestHumanReadableId: undefined,
      startTime: undefined,
      serviceType: serviceTypeValue ?? undefined,
      taxiClass,
    };

    setQuery(filters);
  };

  return (
    <Modal
      title={t.Requests.Filters.title}
      className={styles.modal}
      visible={isOpened('filters')}
      onCancel={handleCancel}
      onOk={handleSuccess}
      okText={t.global.save}
      cancelText={t.global.cancel}
      cancelButtonProps={{ className: styles.cancelButton }}
      destroyOnClose
    >
      <Form
        form={form}
        onFinish={onFinish}
        initialValues={query}
      >
        <Form.Item
          label={t.Requests.Filters.status}
          name="statuses"
          rules={[ValidationRules.general.required]}
        >
          <StatusSelect
            mode="multiple"
            placeholder={t.Requests.Filters.chooseStatus}
            tripMode={TripTypes.Passenger}
            allowClear
            maxTagCount="responsive"
          />
        </Form.Item>

        <Form.Item label={t.Requests.Filters.startTime} name="startTime">
          <NewDateInput
            withAvialableFuture
            quarterDisabled
            yearsDisabled
            placeholder={t.Requests.Filters.selectStartTime}
          />
        </Form.Item>

        <Form.Item label={t.Requests.Filters.driver} name="driverIds">
          <SelectDrivers
            type={TripTypes.Passenger}
            className={styles.select}
            mode="multiple"
            maxTagCount="responsive"
            allowClear
            size="small"
          />
        </Form.Item>

        <Form.Item
          label={t.Requests.Filters.serviceType}
          name="serviceType"
        >
          <Select
            placeholder={t.Requests.Filters.chooseServiceType}
            allowClear
          >
            {serviceTypeOptions.map(option => (
              <Option key={option.value} value={option.value}>
                {option.label}
              </Option>
            ))}
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  );
};
