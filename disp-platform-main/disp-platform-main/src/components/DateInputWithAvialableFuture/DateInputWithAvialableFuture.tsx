/* eslint-disable @typescript-eslint/no-empty-function */
import React, { useMemo } from 'react';
import { DatePicker, Radio } from 'antd';
import styled from 'styled-components';
import moment, { Moment } from 'moment';
import { RadioChangeEvent } from 'antd/lib/radio';
import { range } from 'd3-array';
import { DateInputProps, Mode } from './types';
import { getFormatDateInput, getFormatRangeInput } from './utils';

const StyledModeSelector = styled.div`
  padding-bottom: var(--padding-base);
`;

const options = [
  { value: 'date', label: 'День' },
  { value: 'range', label: 'Диапазон' },
  { value: 'quarter', label: 'Квартал' },
  { value: 'year', label: 'Год' },
];

const SelectMode: React.FC<{
  value: Mode;
  isNewDesign?: boolean;
  onChange: (mode: Mode) => void;
  yearsDisabled?: boolean;
  quarterDisabled?: boolean;
}> = ({
  value, onChange, yearsDisabled, quarterDisabled, isNewDesign = false,
}) => {
  const shortOptions = useMemo(
    () => options.filter(option => {
      if ((yearsDisabled && option.value === 'year') || (quarterDisabled && option.value === 'quarter')) {
        return false;
      }

      return true;
    }),
    [yearsDisabled, quarterDisabled]
  );

  const handleModeChange = ({ target: { value } }: RadioChangeEvent) => {
    onChange(value);
  };

  return (
    <StyledModeSelector>
      <Radio.Group
        onChange={handleModeChange}
        value={value}
        options={shortOptions}
        optionType="button"
        buttonStyle="solid"
        size={isNewDesign ? 'middle' : 'small'}
      />
    </StyledModeSelector>
  );
};

const StyledDateInput = styled.div`
  display: flex;
  flex-direction: column;
`;

export const DateInputWithAvialableFuture: (props: DateInputProps) => JSX.Element = ({
  value: { mode, value: [start, end] } = { mode: 'date', value: [null, null] },
  onChange = () => {},
  onOpenChange = () => {},
  isDisabledFutureDate = false,
  dateTimeShow = false,
  rangeTimeShow = false,
  yearsDisabled = false,
  quarterDisabled = false,
  showModes = true,
  showToday = true,
  isNewDesign = false,
  className = '',
  dropdownClassName = '',
  placeholder,
}) => {
  const setMode = (mode: Mode) => onChange({ mode, value: [null, null] });

  const handleSingleSelect = (value: Moment | null) => onChange({ mode, value: [value, null] });

  const handleRangeSelect = (values: [Moment | null, Moment | null] | null) => {
    onChange({ mode, value: values || [null, null] });
  };

  const disabledDate = (d: moment.Moment) => {
    const minDate = moment().subtract(5, 'year');
    const maxDate = moment().add(1, 'year').startOf('day');
    return (d && moment(d) < minDate) || moment(d) > maxDate;
  };

  const disabledTime = (d: moment.Moment | null) => {
    const maxHour = d?.clone().endOf('day').isSame(moment().endOf('day')) && moment().hour();
    const maxMinute
      = d?.clone().endOf('day').isSame(moment().endOf('day')) && d?.hour() === moment().hour() && moment().minute();
    return {
      disabledHours: () => (isDisabledFutureDate && maxHour ? range(maxHour, 24) : []),
      disabledMinutes: () => (isDisabledFutureDate && maxMinute ? range(maxMinute, 60) : []),
    };
  };

  const { format, placeholder: defaultPlaceholder } = getFormatDateInput(mode, dateTimeShow, rangeTimeShow, start);

  const rangePlaceholder = (placeholder as [string, string]) ?? getFormatRangeInput(mode, start, end);

  const renderFooter = () => isNewDesign ? (
    <SelectMode
      value={mode}
      isNewDesign={isNewDesign}
      onChange={setMode}
      yearsDisabled={yearsDisabled}
      quarterDisabled={quarterDisabled}
    />
  ) : undefined;

  return (
    <StyledDateInput>
      {showModes && (
        <SelectMode
          value={mode}
          onChange={setMode}
          yearsDisabled={yearsDisabled}
          quarterDisabled={quarterDisabled}
        />
      )}

      {mode === 'date' ? (
        <DatePicker
          inputReadOnly
          showToday={showToday}
          showTime={dateTimeShow}
          disabledDate={disabledDate}
          disabledTime={dateTimeShow ? disabledTime : undefined}
          value={start}
          onChange={handleSingleSelect}
          format={format}
          placeholder={(placeholder as string) ?? defaultPlaceholder}
          className={className}
          dropdownClassName={dropdownClassName}
          renderExtraFooter={renderFooter}
          onOpenChange={onOpenChange}
          getPopupContainer={trigger => trigger.parentNode as HTMLElement}
        />
      ) : mode === 'range' ? (
        <DatePicker.RangePicker
          allowEmpty={[true, true]}
          inputReadOnly
          showTime={rangeTimeShow}
          disabledDate={disabledDate}
          disabledTime={rangeTimeShow ? disabledTime : undefined}
          value={[start, end]}
          onChange={handleRangeSelect}
          format={format}
          placeholder={rangePlaceholder}
          separator={null}
          className={className}
          dropdownClassName={dropdownClassName}
          renderExtraFooter={renderFooter}
          onOpenChange={onOpenChange}
          getPopupContainer={trigger => trigger.parentNode as HTMLElement}
        />
      ) : mode === 'quarter' ? (
        <DatePicker
          picker="quarter"
          inputReadOnly
          disabledDate={disabledDate}
          value={start}
          onChange={handleSingleSelect}
          format={format}
          placeholder={(placeholder as string) ?? defaultPlaceholder}
          defaultPickerValue={moment()}
          className={className}
          dropdownClassName={dropdownClassName}
          renderExtraFooter={renderFooter}
          onOpenChange={onOpenChange}
          getPopupContainer={trigger => trigger.parentNode as HTMLElement}
        />
      ) : (
        <DatePicker
          picker="year"
          value={start}
          disabledDate={disabledDate}
          onChange={handleSingleSelect}
          format={format}
          placeholder={(placeholder as string) ?? defaultPlaceholder}
          className={className}
          dropdownClassName={dropdownClassName}
          renderExtraFooter={renderFooter}
          onOpenChange={onOpenChange}
          getPopupContainer={trigger => trigger.parentNode as HTMLElement}
        />
      )}
    </StyledDateInput>
  );
};
