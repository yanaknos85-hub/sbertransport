import React from 'react';
import { Input } from 'antd';
import { FormInstance } from 'antd/lib/form';
import { InputProps } from 'antd/lib/input';

/**
 * Поле ввода которое позволяет вводить только
 * разрешённые символы
 */
export default (
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  props: InputProps & { form: FormInstance; name: string; onClearButtonActivator?: (event: any) => void }
): JSX.Element => {
  const {
    name, type, form, onClearButtonActivator, ...other
  } = props;

  const onChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { value, minLength } = event.target;
    const correctValue = value.replace(/[^a-zA-Z0-9а-яА-ЯЁё@. +,-]/gi, '').replace(/\s+/g, ' ');
    /**
     * Не позволяет отправить пробелы в начале и конце строки минимально заданной длины,
     * предотвращает падение по ошибке при валидации на стороне бэка
     */
    form.setFieldsValue({ [name]: correctValue.length === minLength ? correctValue.trim() : correctValue });

    if (onClearButtonActivator) {
      onClearButtonActivator(event);
    }
  };

  return <Input {...other} onChange={onChange} />;
};
