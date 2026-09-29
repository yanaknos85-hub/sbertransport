import { ChangeEvent } from 'react';
import { compose } from 'ramda';
import { FormInstance } from 'antd/lib/form';
import { Query } from '../types';

const getValue = (e: ChangeEvent<HTMLInputElement>) => e.target.value;

export const onChange = (callback: (value: string) => void): ((e: ChangeEvent<HTMLInputElement>) => void) => (
  compose(callback, getValue)
);

export const defaultHandleSearch = (setQuery: (query: Query) => void)
  : ((value: string) => void) => (value: string): void => {
  const isName = /[^\d]/.test(value);
  const nameParts = value.split(' ');
  const newQuery: Partial<Query> = isName
    ? {
      lastName: nameParts[0], firstName: nameParts[1], patronymic: nameParts[2],
    }
    : {
      humanReadableId: value,
    };

  setQuery(newQuery);
};

export const resetFormFields = (form: FormInstance) => {
  const fieldValues = form.getFieldsValue() ?? {};

  form.setFieldsValue(Object.fromEntries(Object.keys(fieldValues).map(key => [key, undefined])));
};
