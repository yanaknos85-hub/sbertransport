/* eslint-disable jsx-a11y/label-has-for */
import React from 'react';
import cn from 'classnames';
import { DateInputProps } from 'components/DateInput/types';
import { DateInputProps as AvialableFutureDateInputProps } from 'components/DateInputWithAvialableFuture/types';
import { DateInput } from 'components/DateInput/DateInput';
import { DateInputWithAvialableFuture } from '../DateInputWithAvialableFuture/DateInputWithAvialableFuture';

import styles from './DatePicker.module.scss';

type NewDatePickerProps = {
  label?: string;
  withAvialableFuture?: boolean;
} & DateInputProps &
AvialableFutureDateInputProps;

export const NewDateInput: (props: NewDatePickerProps) => JSX.Element = props => {
  const DatePicker = props.withAvialableFuture ? DateInputWithAvialableFuture : DateInput;
  const isRangeMode = props.value?.mode === 'range';

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
          [styles.dateQuarterPicker]: props.value?.mode === 'quarter',
          [styles.dateYearPicker]: props.value?.mode === 'year',
          [styles.inActiveLabel]: !props.value?.value[0] && !props.value?.value[1],
          [styles.placeholderRange]:
            props.placeholder && isRangeMode && (!props.value?.value[0] || !props.value?.value[1]),
        })}
        dropdownClassName={styles.dropdownDatePicker}
      />
    </div>
  );
};
