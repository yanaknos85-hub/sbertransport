import React from 'react';
import { DatePicker } from 'antd';
import styled from 'styled-components';
import moment, { Moment } from 'moment';
import { MinusOutlined } from '@ant-design/icons';
import { DateInputProps, Mode } from './types';
import { getFormatDateInput, getFormatRangeInput } from './utils';
import { SelectMode } from './SelectMode';

const StyledDateInput = styled.div`
  display: flex;
  flex-direction: column;
`;

export const DateInput: (props: DateInputProps) => JSX.Element = ({
  value: { mode, value: [start, end] } = { mode: 'date', value: [null, null] },
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  onChange = () => {},
  isDisabledFutureDate = false,
  dropdownClassName,
}) => {
  const setMode = (mode: Mode) => onChange({ mode, value: [null, null] });

  const handleSingleSelect = (value: Moment | null) => onChange({ mode, value: [value, null] });

  const handleRangeSelect = (values: [Moment | null, Moment | null] | null) => {
    onChange({ mode, value: values || [null, null] });
  };

  const disabledDate = (d: moment.Moment) => isDisabledFutureDate && d.isAfter(moment().endOf('day'));

  const { format, placeholder } = getFormatDateInput(mode, start);

  const rangePlaceholder = getFormatRangeInput(mode, start, end);

  return (
    <StyledDateInput>
      <SelectMode value={mode} onChange={setMode} />
      {mode === 'date' ? (
        <DatePicker
          inputReadOnly
          showToday
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          value={start as any}
          disabledDate={disabledDate}
          disabledTime={undefined}
          onChange={handleSingleSelect}
          format={format}
          placeholder={placeholder}
          dropdownClassName={dropdownClassName}
          getPopupContainer={trigger => trigger.parentNode as HTMLElement}
        />
      ) : mode === 'range' ? (
        <DatePicker.RangePicker
          allowEmpty={[true, true]}
          inputReadOnly
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          value={[start as any, end as any]}
          disabledDate={disabledDate}
          disabledTime={undefined}
          onChange={handleRangeSelect}
          format={format}
          placeholder={rangePlaceholder}
          dropdownClassName={dropdownClassName}
          separator={<MinusOutlined style={{ transform: 'scale(0.5)' }} />}
          getPopupContainer={trigger => trigger.parentNode as HTMLElement}
        />
      ) : mode === 'quarter' ? (
        <DatePicker
          picker="quarter"
          inputReadOnly
          disabledDate={disabledDate}
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          value={start as any}
          onChange={handleSingleSelect}
          format={format}
          placeholder={placeholder}
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          defaultPickerValue={moment() as any}
          dropdownClassName={dropdownClassName}
          getPopupContainer={trigger => trigger.parentNode as HTMLElement}
        />
      ) : (
        <DatePicker
          picker="year"
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          value={start as any}
          onChange={handleSingleSelect}
          format={format}
          placeholder={placeholder}
          dropdownClassName={dropdownClassName}
          getPopupContainer={trigger => trigger.parentNode as HTMLElement}
        />
      )}
    </StyledDateInput>
  );
};
