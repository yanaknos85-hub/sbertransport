import React, { useState } from 'react';
import type { FC } from 'react';
import {
  DatePicker, Form, Radio, Tooltip
} from 'antd';
import type { RadioChangeEvent } from 'antd';
import locale from 'antd/es/date-picker/locale/ru_RU';
import { FormInstance } from 'antd/es/form/Form';
import moment from 'moment';
import type { Moment } from 'moment';
import cn from 'classnames';

import { DATE_FORMAT, DescriptionRadioDateNow } from 'constants/constants.app';

import { validateSelectedDateByPurpose } from 'modules/EmployeeApp/components/CreateTripRequest/utils/utils';

import { TripPurpose } from 'stores/Trip/Trip.interface';

import { TripRequestPurpose } from '../../hooks/useTripRequestPurpose';
import { checkPurposeValidity } from '../../utils/utils';
import { ValidationRules } from 'shared/fieldValidationRules';
import { ReactComponent as IcInfo } from 'shared/components/Images/ic_info.svg';

import styles from './tripRequestDate.module.scss';

export const getDisabledDate = (value: moment.Moment): boolean => {
  const now = moment().startOf('day');
  const maxDate = now.clone().add(1, 'years').endOf('day');
  return value < now || value > maxDate;
};

interface IProps {
  tripPurpose: TripRequestPurpose;
  form: FormInstance;
  disabled: boolean;
  purposes: TripPurpose[];
  style?: React.CSSProperties;
  id?: string;
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export const TripRequestDate: FC<IProps> = ({
  tripPurpose,
  form,
  disabled,
  purposes,
  style,
  id,
}) => {
  const { setPurposeValidity, purposeValidity } = tripPurpose;

  const [datePickerSelectDate, setDatePickerSelectDate] = useState<Moment>(moment());

  const nearestAvailableTime = moment().add(30, 'minutes');

  const onRadioChange = (e: RadioChangeEvent): void => {
    if (e.target.value === 'now') {
      form.setFieldsValue({ date: undefined });
    } else {
      form.setFieldsValue({ date: nearestAvailableTime });
    }

    setPurposeValidity(validateSelectedDateByPurpose(form, purposes, purposeValidity.purposeId));
  };

  const onDatePickerChange = (selectedDate: Moment | null): void => {
    if (selectedDate) {
      // Если выбранная дата раньше текущей + 30 минут, устанавливаем ближайшую доступную
      if (selectedDate.isBefore(nearestAvailableTime, 'minute')) {
        setDatePickerSelectDate(nearestAvailableTime);
        form.setFieldsValue({ date: nearestAvailableTime });
      }
    }
    setPurposeValidity(validateSelectedDateByPurpose(form, purposes, purposeValidity.purposeId));
  };

  const handleRadioNowClick = () => {
    setPurposeValidity(validateSelectedDateByPurpose(form, purposes, purposeValidity.purposeId));
  };

  const disabledHours = () => {
    // Если выбран не текущий день, не блокируем часы
    if (datePickerSelectDate.isAfter(nearestAvailableTime, 'day')) {
      return [];
    }

    return Array.from({ length: 24 }, (_, i) => i).filter(hour => hour < nearestAvailableTime.hour());
  };

  const disabledMinutes = selectedHour => {
    const selectedMoment = moment(datePickerSelectDate).hour(selectedHour);

    // Если выбран не текущий день, не блокируем минуты
    if (selectedMoment.isAfter(nearestAvailableTime, 'day')) {
      return [];
    }

    // Если час больше текущего, не блокируем минуты
    if (selectedHour > nearestAvailableTime.hour()) {
      return [];
    }

    // Если час равен текущему, блокируем минуты до текущей
    return Array.from({ length: 60 }, (_, i) => i).filter(minute => minute < nearestAvailableTime.minute());
  };

  return (
    <div
      className={styles.tripRequestDate}
      style={style}
      id={id}
    >
      <Form.Item name="when">
        <Radio.Group
          className={styles.radioGroup}
          onChange={onRadioChange}
          disabled={disabled}
        >
          <Radio className={styles.radio_date} value="notnow">
            Запланировать
          </Radio>
          <Radio
            className={styles.radio_date}
            onClick={handleRadioNowClick}
            value="now"
          >
            {' '}
            Как можно скорее
            <Tooltip placement="top" title={DescriptionRadioDateNow}>
              <IcInfo className={styles.ic_info} />
            </Tooltip>
          </Radio>
        </Radio.Group>
      </Form.Item>

      <Form.Item
        noStyle={true}
        shouldUpdate={(prevValues, currentValues): boolean => prevValues.when !== currentValues.when}
      >
        {({ getFieldValue }) => {
          return (
            getFieldValue('when') === 'notnow' && (
              <Form.Item
                name="date"
                rules={[
                  { required: true, message: 'Пожалуйста, выберите дату поездки' },
                  ValidationRules.general.isValidTripRequestDate(),
                  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
                  () => ({
                    // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
                    validator() {
                      // FIXME @typescript-eslint/explicit-function-return-type
                      return checkPurposeValidity(purposeValidity.isValid);
                    },
                  }),
                ]}
              >
                <DatePicker
                  defaultValue={nearestAvailableTime}
                  allowClear={false}
                  style={{ width: '100%' }}
                  className={cn('picker', styles.chemodanColorTest)}
                  locale={locale}
                  onChange={onDatePickerChange}
                  onSelect={setDatePickerSelectDate}
                  showTime={{ hideDisabledOptions: true }}
                  format={DATE_FORMAT.DATE_WITH_TIME_OTHER}
                  placeholder="Выберите дату и время"
                  dropdownClassName="no-now-btn"
                  disabledDate={getDisabledDate}
                  disabledTime={() => ({ disabledHours, disabledMinutes })}
                  disabled={disabled}
                  getPopupContainer={trigger => trigger.parentNode as HTMLElement}
                />
              </Form.Item>
            )
          );
        }}
      </Form.Item>
    </div>
  );
};
