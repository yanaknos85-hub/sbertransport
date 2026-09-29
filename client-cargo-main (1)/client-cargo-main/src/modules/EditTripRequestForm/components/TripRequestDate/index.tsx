import React from 'react';
import { DatePicker, Form, Radio } from 'antd';
import locale from 'antd/es/date-picker/locale/ru_RU';
import { FormInstance } from 'antd/es/form/Form';
import moment from 'moment';

import { TripPurpose } from 'stores/Trip/Trip.interface';
import { DATE_FORMAT } from 'constants/constants.app';
import { validateSelectedDateByPurpose } from 'modules/CreateTripRequest/utils/utils';

import { TripRequestPurpose } from '../../hooks/useTripRequestPurpose';
import { checkPurposeValidity } from '../../utils/utils';

type TripRequestDate = 'now' | 'notnow';

export const getDisabledDate = (value: moment.Moment): boolean => {
  const now = moment().add(-1, 'days');
  return value < now || value > now.add(1, 'years');
};

export const TripRequestDate = ({
  tripPurpose,
  form,
  disabled,
  purposes,
  style,
  id,
}: {
  tripPurpose: TripRequestPurpose;
  form: FormInstance;
  disabled: boolean;
  purposes: TripPurpose[];
  style?: React.CSSProperties;
  id?: string;
}): JSX.Element => {
  const { setPurposeValidity, purposeValidity } = tripPurpose;
  const onDateChange = (): void => {
    setPurposeValidity(validateSelectedDateByPurpose(form, purposes, purposeValidity.purposeId));
  };

  const handleRadioNowClick = () => {
    form.resetFields(['purpose']);
    setPurposeValidity({ purposeId: '', isValid: true });
  };

  return (
    <div style={style} id={id}>
      <Form.Item name="when">
        <Radio.Group disabled={disabled}>
          <Radio onClick={handleRadioNowClick} value="now">
            Поеду сейчас
          </Radio>
          <Radio onClick={handleRadioNowClick} value="notnow">
            Запланирую
          </Radio>
        </Radio.Group>
      </Form.Item>

      <Form.Item
        noStyle={true}
        shouldUpdate={(prevValues, currentValues): boolean => prevValues.when !== currentValues.when}
      >
        {({ getFieldValue }) => getFieldValue('when') === 'notnow' && (
        <Form.Item
          name="date"
          rules={[
            { required: true, message: 'Пожалуйста, выберите цель поездки' },
            // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
            () => ({
              // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
              validator() {
                return checkPurposeValidity(purposeValidity.isValid);
              },
            }),
          ]}
        >
          <DatePicker
            style={{ width: '100%' }}
            className="picker"
            locale={locale}
            onChange={onDateChange}
            showTime={true}
            format={DATE_FORMAT.DATE_WITH_TIME}
            placeholder="Выберите дату и время"
            dropdownClassName="no-now-btn"
            disabledDate={getDisabledDate}
            disabled={disabled}
            getPopupContainer={trigger => trigger.parentNode as HTMLElement}
          />
        </Form.Item>
        )}
      </Form.Item>
    </div>
  );
};
