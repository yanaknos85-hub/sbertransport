import React, { Form, Modal } from 'antd';
import { FC, useEffect } from 'react';
import { useTranslation } from 'i18n';
import { useForm } from 'antd/lib/form/Form';
import { StatusSelect } from 'components/StatusSelect';
import { useTripsModal } from '../../context/TripsModal';
import styles from './index.module.scss';
import { NewDateInput } from 'components/DatePicker/DatePicker';
import { Store } from 'antd/lib/form/interface';
import moment, { Moment } from 'moment';
import { useTripsQuery } from 'modules/Trips/context/TripsQuery';
import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';
import { TripTypes } from 'constants/app.constants';

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
      desireDateStart, desireDateEnd, ...other
    } = query;

    form.setFieldsValue({
      ...other,
      startTime: {
        mode: Math.abs(moment(desireDateEnd).diff(moment(desireDateStart))) > ONE_DAY ? 'range' : 'date',
        value: [
          desireDateStart ? moment(desireDateStart) : null,
          desireDateEnd ? moment(desireDateEnd) : null,
        ],
      },
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query, form, isOpened('filters')]);

  const onFinish = (values: Store) => {
    const [desireDateStart, desireDateEnd] = values.startTime?.value as (Moment | null)[];
    const endDate = desireDateEnd ?? desireDateStart;

    const filters = {
      statuses: values.statuses,
      desireDateStart: desireDateStart?.clone().startOf('day').utc().format() ?? undefined,
      desireDateEnd: endDate?.clone().endOf('day').utc().format() ?? undefined,
      requestHumanReadableId: undefined,
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
            tripMode={TripTypes.Cargo}
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
      </Form>
    </Modal>
  );
};
