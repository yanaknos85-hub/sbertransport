/* eslint-disable jsx-a11y/label-has-for */
import React from 'react';
import cn from 'classnames';
import { DateInputProps } from 'shared/components/DateInput/types';
import { DateInputProps as AvailableFutureDateInputProps } from 'shared/components/DateInputWithAvailableFuture/types';
import { DateInput } from 'shared/components/DateInput/DateInput';
import { DateInputWithAvailableFuture } from '../DateInputWithAvailableFuture/DateInputWithAvailableFuture';

import styles from './DatePicker.module.scss';

type NewDatePickerProps = {
  label?: string;
  withAvailableFuture?: boolean;
} & DateInputProps &
AvailableFutureDateInputProps;

export const NewDateInput: (props: NewDatePickerProps) => JSX.Element = props => {
  const DatePicker = props.withAvailableFuture ? DateInputWithAvailableFuture : DateInput;
  const isRangeMode = props.value && typeof props.value === 'object' && 'mode' in props.value
    ? props.value.mode === 'range'
    : false;

  const hasValue = props.value && typeof props.value === 'object' && 'value' in props.value
    ? Array.isArray(props.value.value) && props.value.value.length > 0
    : false;

  const hasBothRangeValues = props.value && typeof props.value === 'object' && 'value' in props.value
    ? Array.isArray(props.value.value) && props.value.value.length === 2 && props.value.value[0] && props.value.value[1]
    : false;

  return (
    <div className={styles.dateInputContainer}>
      {props.label && <label className={styles.dateInputLabel}>{props.label}</label>}
      <DatePicker
        {...props}
        isNewDesign
        showToday={false}
        showModes={false}
        placeholder={props.placeholder && isRangeMode ? ['Начальная дата', 'Конечная дата'] : props.placeholder}
        className={cn(styles.datePicker, {
          [styles.labeledDatePicker]: props.label,
          [styles.dateRangePicker]: isRangeMode,
          [styles.dateQuarterPicker]: props.value && typeof props.value === 'object' && props.value.mode === 'quarter',
          [styles.dateYearPicker]: props.value && typeof props.value === 'object' && props.value.mode === 'year',
          [styles.inActiveLabel]: !hasValue,
          [styles.placeholderRange]: props.placeholder && isRangeMode && !hasBothRangeValues,
        })}
        dropdownClassName={styles.dropdownDatePicker}
      />
    </div>
  );
};
