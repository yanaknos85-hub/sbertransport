/* eslint-disable @typescript-eslint/no-empty-function */
import React, { FC, useState, useMemo } from 'react';
import { DatePicker, Radio } from 'antd';
import styled from 'styled-components';
import moment, { Moment } from 'moment';
import { RadioChangeEvent } from 'antd/lib/radio';
import { range } from 'd3-array';
import Select, { LabeledValue } from 'antd/lib/select';
import { DateInputProps, Mode } from './types';
import { getFormatDateInput, getFormatRangeInput, toWeekRange } from './utils';

const StyledModeSelector = styled.div`
  padding-bottom: var(--padding-base);
`;

const options = [
  { value: 'date', label: 'День' },
  { value: 'range', label: 'Диапазон' },
  { value: 'quarter', label: 'Квартал' },
  { value: 'year', label: 'Год' },
  { value: 'weeks', label: 'Периоды' },
];

const SelectMode: FC<{
  value: Mode;
  onChange: (mode: Mode) => void;
  yearsDisabled?: boolean;
  quarterDisabled?: boolean;
  weeksEnabled?: boolean;
  isNewDesign?: boolean;
}> = ({
  value, onChange, yearsDisabled, quarterDisabled, weeksEnabled = false, isNewDesign = false,
}) => {
  const handleModeChange = ({ target: { value } }: RadioChangeEvent) => {
    onChange(value);
  };

  const optionsMods = useMemo(
    () => options.filter(option => {
      if (
        (yearsDisabled && option.value === 'year')
        || (quarterDisabled && option.value === 'quarter')
        || (!weeksEnabled && option.value === 'weeks')
      ) {
        return false;
      }

      return true;
    }),
    [yearsDisabled, quarterDisabled, weeksEnabled]
  );

  return (
    <StyledModeSelector>
      <Radio.Group
        onChange={handleModeChange}
        value={value}
        options={optionsMods}
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

export const DateInput: FC<DateInputProps> = ({
  value,
  onChange = () => {},
  onOpenChange = () => {},
  isDisabledFutureDate = false,
  dateTimeShow = false,
  rangeTimeShow = false,
  yearsDisabled = false,
  quarterDisabled = false,
  weeksEnabled = false,
  showModes = true,
  showToday = true,
  isNewDesign = false,
  className = '',
  dropdownClassName = '',
  placeholder,
}) => {
  const safeValue = value || { mode: 'date', value: [null, null] };
  const mode = safeValue.mode || 'date';
  const start = safeValue.value?.[0] || null;
  const end = safeValue.value?.[1] || null;

  const setMode = (newMode: Mode) => onChange({ mode: newMode, value: [null, null] });

  const handleSingleSelect = (selectedValue: Moment | null) => {
    onChange({ mode, value: [selectedValue, null] });
  };

  const handleRangeSelect = (values: [Moment | null, Moment | null] | null) => {
    onChange({ mode, value: values || [null, null] });
  };

  const disabledDate = (d: moment.Moment) => isDisabledFutureDate && d.isAfter(moment().endOf('day'));

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

  const getWeeks = (): LabeledValue[] => {
    const weeksOptions: LabeledValue[] = [];

    for (let i = 0; i < 7; i++) {
      const month = moment().clone().subtract(i, 'M');

      const firstDay = month.clone().startOf('month');
      const lastDay = month.clone().endOf('month');

      const weeks = [
        { start: firstDay.clone(), end: firstDay.clone().add(7, 'd') },
        { start: firstDay.clone().add(7, 'd'), end: firstDay.clone().add(15, 'd') },
        { start: firstDay.clone().add(15, 'd'), end: firstDay.clone().add(23, 'd') },
        { start: firstDay.clone().add(23, 'd'), end: lastDay.clone() },
      ];

      weeks.forEach(week => {
        weeksOptions.push({
          value: `{ "start": "${week.start}", "end": "${week.end}" }`,
          label: `${week.start.format('YYYY.MM.DD')} — ${week.end.clone().subtract(1).format('DD')}`,
        });
      });
    }

    return weeksOptions;
  };

  const weekRange = toWeekRange();
  const [weekValue, setWeekValue] = useState(`{ "start": "${weekRange.value[0]}", "end": "${weekRange.value[1]}" }`);

  const renderFooter = () => isNewDesign ? (
    <SelectMode
      value={mode}
      onChange={setMode}
      yearsDisabled={yearsDisabled}
      quarterDisabled={quarterDisabled}
      weeksEnabled={weeksEnabled}
      isNewDesign={isNewDesign}
    />
  ) : undefined;

  const renderDatePicker = () => {
    switch (mode) {
      case 'date':
        return (
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
        );

      case 'range':
        return (
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
        );

      case 'quarter':
        return (
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
        );

      case 'weeks':
        if (!weeksEnabled) return null;
        return (
          <Select
            options={getWeeks()}
            placeholder="Выберите период выплаты"
            onChange={selectedValue => {
              try {
                const parsedValue = JSON.parse(selectedValue as string);
                setWeekValue(`{ "start": "${parsedValue.start}", "end": "${parsedValue.end}" }`);
                onChange({ mode: 'weeks', value: [parsedValue.start, parsedValue.end] });
              } catch (error) {
                console.error('Error parsing week value:', error);
              }
            }}
            value={weekValue}
            allowClear
            onClear={() => {
              setWeekValue('');
              onChange({ mode: 'weeks', value: [null, null] });
            }}
          />
        );

      case 'year':
        return (
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
        );

      default:
        return null;
    }
  };

  return (
    <StyledDateInput>
      {showModes && (
        <SelectMode
          value={mode}
          onChange={setMode}
          yearsDisabled={yearsDisabled}
          quarterDisabled={quarterDisabled}
          weeksEnabled={weeksEnabled}
        />
      )}
      {renderDatePicker()}
    </StyledDateInput>
  );
};
