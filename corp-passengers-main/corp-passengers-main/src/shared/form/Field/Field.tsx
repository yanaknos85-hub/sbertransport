import React, { FC, useRef } from 'react';
import uuid from 'utils/uuid';

import { Content, Field, Label } from './Field.style';

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
  department = 'department',
  address = 'address',
  cargoType = 'cargoType',
  rate = 'rate',
  range = 'range',
}

interface Props {
  label?: React.ReactNode;
  children: JSX.Element;
  id?: number;
  type?: FieldType;
}

const FieldComponent: FC<Props> = ({
  id: fieldId, label, type, children,
}) => {
  const nanoIdRef = useRef(uuid());
  const id = fieldId ?? nanoIdRef.current;
  const isCheckboxType = type && [FieldType.checkbox].includes(type);
  const fieldElement = React.cloneElement(children, { id });

  return (
    <Field checkboxed={isCheckboxType}>
      {label && !isCheckboxType && <Label htmlFor={id}>{label}</Label>}
      <Content>{fieldElement}</Content>
    </Field>
  );
};

export default FieldComponent;
