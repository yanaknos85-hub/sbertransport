import React from 'react';
import { Input } from 'antd';
import { FormInstance } from 'antd/lib/form';
import { InputProps } from 'antd/lib/input';
import CustomInput from 'shared/form/Input/Input';

/**
 * Поле ввода которое позволяет вводить только
 * разрешённые символы
 */
export default (
  props: InputProps & {
    form: FormInstance;
    name: string;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    onClearButtonActivator?: (event: any) => void;
    allowURL?: boolean;
    isNewDesign?: boolean;
  }
): JSX.Element => {
  const {
    name, type, form, onClearButtonActivator, allowURL, isNewDesign, ...other
  } = props;

  const onChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { value } = event.target;
    const allowedSymbols = /[^a-zA-Z0-9а-яА-ЯЁё@. +,_,-]/gi;
    const correctValue = allowURL ? value : value.replace(allowedSymbols, '').replace(/\s+/g, ' ');
    form.setFieldsValue({ [name]: correctValue || undefined });

    if (onClearButtonActivator) {
      onClearButtonActivator(event);
    }
  };

  const onBlur = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { value } = event.target;
    /**
     * Убирает пробели в начале и в конце при onBlur
     * */
    form.setFieldsValue({ [name]: value.trim() || undefined });

    if (onClearButtonActivator) {
      onClearButtonActivator(event);
    }
  };

  if (isNewDesign) {
    return (
      <CustomInput
        {...other}
        onChange={onChange}
        onBlur={onBlur}
      />
    );
  }

  return (
    <Input
      {...other}
      onChange={onChange}
      onBlur={onBlur}
    />
  );
};
