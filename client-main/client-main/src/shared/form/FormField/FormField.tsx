/* eslint-disable react/destructuring-assignment */
import { Form } from 'antd';
import React, { FC } from 'react';

import Address from '../Address/Address';
import CargoType from '../CargoType/CargoType';
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
import { FormFieldWrapper } from './FormField.style';

export interface FormFieldType {
  type?: FieldType;
  error?: string;
  index?: number;
  params?: Record<string, any>;
  [x: string]: any;
}

const FormField: FC<FormFieldType> = ({
  type = FieldType.input,
  error,
  index,
  params, // Api элемента формы
  ...rest // Api Form.Item (https://ant.design/components/form/#Form.Item)
}) => (
  <FormFieldWrapper>
    <Field
      label={rest.label}
      type={type}
      error={error}
    >
      <Form.Item>
        <Form.Item
          noStyle={true}
          {...(type === FieldType.checkbox ? { valuePropName: 'checked' } : {})}
          {...rest}
        >
          {
            (
              {
                [FieldType.input]: <Input {...params} />,
                [FieldType.textarea]: <Textarea {...params} />,
                [FieldType.number]: <InputNumber {...params} />,
                [FieldType.checkbox]: <Checkbox {...params}>{rest.label}</Checkbox>,
                [FieldType.radio]: <Radio {...params} />,
                [FieldType.select]: <Select {...params} />,
                [FieldType.date]: <DatePicker {...params} />,
                [FieldType.employee]: <Employee index={index} {...params} />,
                [FieldType.address]: <Address index={index} {...params} />,
                [FieldType.cargoType]: <CargoType {...params} />,
                [FieldType.phone]: (
                  <MaskedInput
                    mask="+7 (111) 111 11 11"
                    value={rest.initialValue}
                    {...params}
                  />
                ),
                [FieldType.rate]: <Rate {...params} />,
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

export default FormField;
