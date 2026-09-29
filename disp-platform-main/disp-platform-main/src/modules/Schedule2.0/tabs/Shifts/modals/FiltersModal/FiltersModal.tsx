import React, { FC, useEffect, useState } from 'react';

import { DatePicker } from 'antd';
import Form from 'antd/es/form';
import { useForm } from 'antd/es/form/Form';
import { PickerProps } from 'antd/lib/date-picker/generatePicker';
import { Store } from 'antd/lib/form/interface';
import Modal from 'antd/lib/modal/Modal';
import moment, { Moment } from 'moment';

import { ValidationRules } from 'utils/fieldValidationRules/fieldValidationRules';

import { useTranslation } from 'i18n';

import { useModals } from '../../../../context/modal.context';
import { useShiftsQuery } from '../../context/shiftsQuery.context';

import styles from './FiltersModal.module.scss';

const { validatorStartDate } = ValidationRules.general;
const { validatorEndDate } = ValidationRules.general;

const DatePickerAutoAccept = (props: PickerProps<Moment>) => {
  const { format, onChange } = props;
  const onBlur = (elem: React.FocusEvent<HTMLInputElement>) => {
    const value = moment(elem.target.value, format as string);
    if (value && value.isValid() && onChange) {
      onChange(value, elem.target.value);
    }
  };
  return <DatePicker {...props} onBlur={onBlur} />;
};

const getISODate = (date: string) => moment(date).toISOString();

export const FiltersModal: FC = () => {
  const [form] = useForm();
  const { t } = useTranslation();
  const { isFiltersOpened, handleClose } = useModals();
  const { query, setQuery } = useShiftsQuery();
  const [initialValues] = useState({
    startDate: moment(query.startDate),
    endDate: moment(query.endDate),
  });

  useEffect(() => {
    form.setFieldsValue({
      startDate: moment(query.startDate),
      endDate: moment(query.endDate),
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query.startDate, query.endDate]);

  const handleSuccess = () => {
    form.submit();
  };

  const handleFinish = (values: Store) => {
    setQuery({
      startDate: getISODate(values.startDate),
      endDate: getISODate(values.endDate.endOf('day')),
    });
    handleClose();
  };

  const handleReset = () => {
    const startDate = moment().startOf('day');
    const endDate = moment().endOf('month');

    setQuery({
      ...query,
      startDate: startDate.toISOString(),
      endDate: endDate.toISOString(),
    });
    form.setFieldsValue({ startDate, endDate });
  };

  const handelCancel = (e: React.MouseEvent<HTMLElement, MouseEvent>) => {
    const target = e.target as HTMLElement;

    // Сбрасываем фильтры только при нажатии на кнопку сброса
    if (target.innerText === t.global.drop) {
      handleReset();
    }

    handleClose();
  };

  return (
    <Modal
      visible={isFiltersOpened}
      className={styles.modal}
      title={t.Shifts.filters}
      onCancel={handleClose}
      okText={t.global.save}
      onOk={handleSuccess}
      cancelText={t.global.drop}
      cancelButtonProps={{ className: styles.cancelButton, onClick: handelCancel }}
    >
      <Form
        className={styles.form}
        form={form}
        onFinish={handleFinish}
        initialValues={initialValues}
      >
        <Form.Item
          className={styles.formItem}
          name="startDate"
          label={t.Shifts.startDate}
          rules={[validatorStartDate(form, 'endDate', true)]}
        >
          <DatePickerAutoAccept
            className={styles.input}
            showSecond={false}
            format="DD.MM.YYYY"
            showToday
            clearIcon={null}
          />
        </Form.Item>

        <Form.Item dependencies={['startDate']}>
          {({ getFieldValue }) => (
            <Form.Item
              className={styles.formItem}
              name="endDate"
              label={t.Shifts.endDate}
              rules={[validatorEndDate(form)]}
            >
              <DatePickerAutoAccept
                className={styles.input}
                showSecond={false}
                format="DD.MM.YYYY"
                showToday
                clearIcon={null}
                disabledDate={current => current && (
                  current > (getFieldValue('startDate') as Moment).clone().add(1, 'month')
                  || current < getFieldValue('startDate')
                )}
              />
            </Form.Item>
          )}
        </Form.Item>
      </Form>
    </Modal>
  );
};
