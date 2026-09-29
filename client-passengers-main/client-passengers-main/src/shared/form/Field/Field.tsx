import { nanoid } from 'nanoid';
import React, { FC, useRef } from 'react';

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
  date = 'date',
  employee = 'employee',
  address = 'address',
  cargoType = 'cargoType',
  rate = 'rate',
  dragger = 'dragger',
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
  const withLabel = label && type && ![FieldType.checkbox, FieldType.dragger].includes(type);
  const fieldElement = React.cloneElement(children, { id });

  return (
    <Field checkboxed={isCheckboxType}>
      {withLabel && !isCheckboxType && <Label htmlFor={id}>{label}</Label>}
      <Content>{fieldElement}</Content>
      {error && <Error>{error}</Error>}
    </Field>
  );
};

export default FieldComponent;
