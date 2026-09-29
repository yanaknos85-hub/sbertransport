/* eslint-disable react/destructuring-assignment */
import { Form } from 'antd';
import React, { FC } from 'react';

import Address from '../Address/Address';
import Checkbox from '../Checkbox/Checkbox';
import DatePicker from '../DatePicker/DatePicker';
import Employee from '../Employee/Employee';
import Field, { FieldType } from '../Field/Field';
import Input from '../Input/Input';
import InputNumber from '../InputNumber/InputNumber';
import MaskedInput from '../MaskedInput/MaskedInput';
import Radio from '../Radio/Radio';
import Rate from '../Rate/Rate';
import Select from '../Select/Select';
import Textarea from '../Textarea/Textarea';
import Dragger from '../Upload/Dragger/Dragger';
import { FormFieldWrapper } from './FormField.style';

const normalizeReg = (value: string, prevValue: string, normalizeReg) => {
  if (normalizeReg) {
    return value.trim();
  }

  return value;
};

export interface FormFieldType {
  type?: FieldType;
  error?: string;
  index?: number;
  params?: Record<string, any>;
  upperCase?: boolean;
  placeholderText?: string;
  withoutSpaces?: boolean;
  [x: string]: any;
}

const FormField: FC<FormFieldType> = ({
  type = FieldType.input,
  className,
  error,
  index,
  params, // Api элемента формы
  upperCase,
  placeholderText,
  withoutSpaces,
  ...rest // Api Form.Item (https://ant.design/components/form/#Form.Item)
}) => {
  let extraItemProps = {};

  if (type === FieldType.checkbox) {
    extraItemProps = { valuePropName: 'checked' };
  }
  if (type === FieldType.dragger) {
    extraItemProps = {
      valuePropName: 'fileList', getValueFromEvent: (e: any) => {
        if (Array.isArray(e)) {
          return e;
        }
        return e?.fileList;
      },
    };
  }

  return (
    <FormFieldWrapper className={className}>
      <Field
        label={rest.label}
        type={type}
        error={error}
      >
        <Form.Item>
          <Form.Item
            normalize={(value, prevValue) => normalizeReg(value, prevValue, withoutSpaces)}
            validateTrigger={['onChange', 'onBlur']}
            noStyle={true}
            {...extraItemProps}
            {...rest}
          >
            {
              (
                {
                  [FieldType.input]: <Input
                    placeholderText={placeholderText}
                    upperCase={upperCase}
                    {...params}
                                     />,
                  [FieldType.textarea]: <Textarea {...params} />,
                  [FieldType.number]: <InputNumber placeholderText={placeholderText} {...params} />,
                  [FieldType.checkbox]: <Checkbox {...params}>{rest.label}</Checkbox>,
                  [FieldType.radio]: <Radio {...params} />,
                  [FieldType.select]: <Select {...params} />,
                  [FieldType.date]: <DatePicker {...params} />,
                  [FieldType.employee]: <Employee index={index} {...params} />,
                  [FieldType.address]: <Address index={index} {...params} />,
                  [FieldType.phone]: (
                    <MaskedInput
                      mask="+7 (111) 111 11 11"
                      value={rest.initialValue}
                      {...params}
                    />
                  ),
                  [FieldType.rate]: <Rate {...params} />,
                  [FieldType.dragger]: <Dragger {...params}>{rest.label}</Dragger>,
                } as {
                  [key in FieldType]?: JSX.Element;
                }
              )[type]
            }
          </Form.Item>
        </Form.Item>
      </Field>
    </FormFieldWrapper>
  );
};

export default FormField;
