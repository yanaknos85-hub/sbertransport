import React from 'react';
import { DatePicker } from 'antd';
import styled from 'styled-components';
import moment, { Moment } from 'moment';
import { range } from 'd3-array';
import { MinusOutlined } from '@ant-design/icons';
import { DateInputProps } from './types';
import { getFormatDateInput, getFormatRangeInput } from './utils';
import { ReactComponent as Icon } from 'shared/icons/calendar.svg';
import styles from './dateInput.module.scss';

const StyledDateInput = styled.div`
  display: flex;
  flex-direction: column;
`;

// TODO: требуется рефакторинг, компонент скопирован с изменением дизайна в рамках 9368
export const DatePickerRange: (props: DateInputProps) => JSX.Element = ({
  value: { mode, value: [start, end] } = { mode: 'range', value: [null, null] },
  onChange = () => {},
  isDisabledFutureDate = false,
  dateTimeShow = false,
  rangeTimeShow = false,
  allowEmpty = true,
}) => {
  const handleRangeSelect = (values: [Moment | null, Moment | null] | null) => {
    onChange({ mode, value: values || [null, null] });
  };

  const disabledDate = (d: moment.Moment) => isDisabledFutureDate && d.isAfter(moment().endOf('day'));

  const disabledTime = (d: moment.Moment | null) => {
    // TODO refactoring
    const maxHour = d?.clone().endOf('day').isSame(moment().endOf('day')) && moment().hour();
    const maxMinute
      = d?.clone().endOf('day').isSame(moment().endOf('day')) && d?.hour() === moment().hour() && moment().minute();
    return {
      disabledHours: () => (isDisabledFutureDate && maxHour ? range(maxHour, 24) : []),
      disabledMinutes: () => (isDisabledFutureDate && maxMinute ? range(maxMinute, 60) : []),
    };
  };

  const { format } = getFormatDateInput('range', dateTimeShow, rangeTimeShow, start);

  const rangePlaceholder = getFormatRangeInput('range', start, end);

  return (
    <StyledDateInput>
      <DatePicker.RangePicker
        // @ts-ignore
        allowEmpty={allowEmpty && [true, true]}
        inputReadOnly
        showTime={rangeTimeShow}
        disabledDate={disabledDate}
        disabledTime={rangeTimeShow ? disabledTime : undefined}
        value={[start, end]}
        onChange={handleRangeSelect}
        format={format}
        placeholder={rangePlaceholder}
        separator={<MinusOutlined style={{ transform: 'scale(0.5)' }} />}
        getPopupContainer={trigger => trigger.parentNode as HTMLElement}
        className={styles.datePicker}
        suffixIcon={<Icon />}
      />
    </StyledDateInput>
  );
};
