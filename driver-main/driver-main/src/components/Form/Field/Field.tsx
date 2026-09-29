import React, { FC, useRef } from 'react';
import cn from 'classnames';

import uuid from 'utils/base/uuid';
import styles from './Field.module.scss';

export enum FieldType {
  Input = 'input',
  Checkbox = 'checkbox',
  Radio = 'radio',
  Select = 'select',
  Phone = 'phone',
  DriverLicense = 'driverLicense',
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
  const isCheckboxType = type && [FieldType.Checkbox].includes(type);
  const fieldElement = React.cloneElement(children, { id });

  return (
    <div
      className={cn('form-field', styles.field, {
        [styles.checkBoxed]: isCheckboxType,
      })}
    >
      {label && !isCheckboxType && (
        <label className={styles.label} htmlFor={id as string}>
          {label}
        </label>
      )}
      {fieldElement}
    </div>
  );
};

export default FieldComponent;
