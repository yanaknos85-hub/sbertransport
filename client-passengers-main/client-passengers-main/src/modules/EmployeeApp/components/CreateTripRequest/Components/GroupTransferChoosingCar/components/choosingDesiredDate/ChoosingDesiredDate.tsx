/* eslint-disable no-unsafe-optional-chaining */
import {
  DatePicker, Form, Switch
} from 'antd';
import { observer } from 'mobx-react';
import React, { FC } from 'react';
import locale from 'antd/es/date-picker/locale/ru_RU';
import moment, { Moment } from 'moment';
import { FormInstance } from 'antd/es/form/Form';

import { DATE_FORMAT } from 'constants/constants.app';

import { ValidationRules } from 'shared/fieldValidationRules';

import styles from './choosingDesiredDate.module.scss';
import '../../override.scss';

interface PassengerInformationProps {
  form: FormInstance;
  setSwitchBookingTime: React.Dispatch<React.SetStateAction<boolean>>;
  setDateBooking: React.Dispatch<React.SetStateAction<Moment | undefined>>;
}

export const ChoosingDesiredDate: FC<PassengerInformationProps> = observer(
  ({
    form, setSwitchBookingTime, setDateBooking,
  }): JSX.Element => {
    const date = form.getFieldValue('date') || moment();

    const getDisabledDate = (value: moment.Moment): boolean => {
      const now = moment().add(-1, 'days');
      return value < now || value > now.add(1, 'years');
    };

    const handleChangeBookingTime = e => {
      setSwitchBookingTime(e);
    };

    const onDateChange = (date: moment.Moment | null): void => {
      if (date !== null) {
        setDateBooking(date);
      }
    };

    return (
      <div className={styles.wrapper_desiredDate}>
        <span className={styles.desiredDate_title}>
          Желаемая дата и время заказа
        </span>
        <div className={styles.desiredDate}>
          <Form.Item
            name="desiredDateGroupTransfer"
            rules={[
              { required: true, message: 'Пожалуйста, выберите дату поездки' },
              ValidationRules.general.isValidTripRequestDate(),
            ]}
            initialValue={date}
          >
            <DatePicker
              style={{ width: '100%' }}
              className={styles.desiredDate_picker}
              locale={locale}
              defaultValue={date}
              value={date}
              onChange={onDateChange}
              showTime={true}
              format={DATE_FORMAT.DATE_WITH_TIME_OTHER}
              placeholder="Выберите дату и время"
              dropdownClassName="no-now-btn"
              disabledDate={getDisabledDate}
              getPopupContainer={trigger => trigger.parentNode as HTMLElement}
            />
          </Form.Item>
        </div>
        <div className={styles.wrapper_bookingTime}>
          <Switch
            defaultChecked={false}
            onChange={handleChangeBookingTime}
          />
          <span>
            Бронирование по времени
          </span>
        </div>
      </div>
    );
  }
);
