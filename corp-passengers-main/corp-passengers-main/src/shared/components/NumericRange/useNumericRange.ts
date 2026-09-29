import { useState } from 'react';
import { ValidationRules } from '../../fieldValidationRules';

export const useNumericRange = (
  bothFieldsRequired: boolean,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onClearButtonActivator?: (event: any) => void,
  defaultValue = false
) => {
  const [checked, onCheckChange] = useState(defaultValue);

  const [startValue, setStartValue] = useState(0);

  const [endValue, setEndValue] = useState(0);

  const onStartChange = (e: string | number | null | undefined) => {
    if (onClearButtonActivator) {
      onClearButtonActivator(e);
    }
    return e ? setStartValue(Number(e)) : setStartValue(0);
  };
  const onEndChange = (e: string | number | null | undefined) => {
    if (onClearButtonActivator) {
      onClearButtonActivator(e);
    }
    return e ? setEndValue(Number(e)) : setEndValue(0);
  };

  const isStartRequired = bothFieldsRequired ? endValue !== 0 && startValue === 0 : false;
  const isEndRequired = bothFieldsRequired ? startValue !== 0 && endValue === 0 : false;
  const checkRequirement = (rule: boolean) => rule
    ? Promise.reject(new Error(ValidationRules.general.required.message))
    : Promise.resolve();

  return {
    checked,
    onCheckChange,
    isStartRequired,
    isEndRequired,
    startValue,
    onStartChange,
    onEndChange,
    checkRequirement,
  };
};
