import React from 'react';
import { FormInstance } from 'antd/lib/form';
import { Form } from 'antd';
import cn from 'classnames';
import { ModelFormFieldProps } from '../ModelFormField';
import styles from '../modelDetail.module.scss';

type FormTextProps = ModelFormFieldProps & {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  convertToString?: (value: any) => string;
};

export default (props: FormTextProps): JSX.Element => {
  const {
    name, label, convertToString,
  } = props;
  const renderText = (form: FormInstance) => {
    const value = form.getFieldValue(name);
    return <span>{convertToString ? convertToString(value) : value}</span>;
  };

  return (
    <Form.Item
      label={label}
      shouldUpdate
      className={cn({ [styles.formItem]: props.isNewDesign })}
      colon={!props.isNewDesign}
    >
      {renderText}
    </Form.Item>
  );
};
