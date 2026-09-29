import React, { FC, useRef } from 'react';
import { nanoid } from 'nanoid';

import {
  Content, Error, Field, Label
} from './Field.style';

export enum FieldType {
  input = 'input',
  textarea = 'textarea',
  number = 'number',
  phone = 'phone',
  checkbox = 'checkbox',
  radio = 'radio',
  select = 'select',
  selectMultiple = 'selectMultiple',
  date = 'date',
  dateRange = 'dateRange',
  employee = 'employee',
  address = 'address',
  cargoType = 'cargoType',
  rate = 'rate',
}

interface Props {
  label?: React.ReactNode;
  error?: string;
  children: JSX.Element;
  id?: number;
  type?: FieldType;
}

const FieldComponent: FC<Props> = ({
  id: fieldId, label, type, error, children,
}) => {
  const nanoIdRef = useRef(nanoid());
  const id = fieldId ?? nanoIdRef.current;
  const isCheckboxType = type && [FieldType.checkbox].includes(type);
  const fieldElement = React.cloneElement(children, { id });

  return (
    <Field checkboxed={isCheckboxType}>
      {label && !isCheckboxType && <Label htmlFor={id}>{label}</Label>}
      <Content>{fieldElement}</Content>
      {error && <Error>{error}</Error>}
    </Field>
  );
};

export default FieldComponent;
