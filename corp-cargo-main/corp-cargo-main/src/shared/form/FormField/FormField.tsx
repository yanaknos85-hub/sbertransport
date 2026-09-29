/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable react/destructuring-assignment */
import { Form } from 'antd';
import React, { FC } from 'react';

import Checkbox from '../Checkbox/Checkbox';
import DatePicker from '../DatePicker/DatePicker';
import Field, { FieldType } from '../Field/Field';
import Input from '../Input/Input';
import InputNumber from '../InputNumber/InputNumber';
import Radio from '../Radio/Radio';
import Select from '../Select/Select';
import SelectMultiple from '../SelectMultiple/SelectMultiple';
import Textarea from '../Textarea/Textarea';
import DateRange from '../DateRange/DateRange';
import Employee from '../Employee/Employee';
import Department from '../Department/Department';

export interface FormFieldType {
  type?: FieldType;
  index?: number;
  params?: Record<string, any>;
  fieldName?: string;
  [x: string]: any;
}

const FormField: FC<FormFieldType> = ({
  type = FieldType.input,
  index,
  params, // Api элемента формы
  fieldName,
  ...rest // Api Form.Item (https://ant.design/components/form/#Form.Item)
}) => (
  <Field label={rest.label} type={type}>
    <Form.Item>
      <Form.Item
        noStyle
        {...(type === FieldType.checkbox ? { valuePropName: 'checked' } : {})}
        {...rest}
      >
        {
          ({
            [FieldType.input]: <Input {...params} />,
            [FieldType.textarea]: <Textarea {...params} />,
            [FieldType.number]: <InputNumber {...params} />,
            [FieldType.checkbox]: <Checkbox {...params}>{rest.label}</Checkbox>,
            [FieldType.radio]: <Radio {...params} />,
            [FieldType.select]: <Select {...params} />,
            [FieldType.selectMultiple]: <SelectMultiple {...params} />,
            [FieldType.date]: <DatePicker {...params} />,
            [FieldType.dateRange]: <DateRange {...params} />,
            [FieldType.employee]: <Employee index={index} {...params} />,
            // eslint-disable-next-line @stylistic/jsx-max-props-per-line
            [FieldType.department]: <Department index={index} fieldName={fieldName} {...params} />,
          } as {
            [key in FieldType]?: JSX.Element;
          })[type]
        }
      </Form.Item>
    </Form.Item>
  </Field>
);

export default FormField;
