/* eslint-disable @typescript-eslint/no-explicit-any */
import { FC } from 'react';
import { Form } from 'antd-mobile';

import Field, { FieldType } from '../Field/Field';
import Input from 'components/Input/input';
import Checkbox from 'components/Checkbox/Checkbox';
import Radio from 'components/Radio/Radio';
import PhoneMask from 'components/Masks/PhoneMask';
import DriverLicenseMask from 'components/Masks/DriverLicenseMask';

export interface FormFieldType {
  FieldType?: FieldType;
  index?: number;
  params?: Record<string, any>;
  fieldName?: string;
  [x: string]: any;
}

const FormField: FC<FormFieldType> = ({
  type = FieldType.Input,
  index,
  params,
  fieldName,
  ...rest
}) => (
  <Field label={rest.label} type={type}>
    <Form.Item>
      <Form.Item
        noStyle
        {...(type === FieldType.Checkbox ? { valuePropName: 'checked' } : {})}
        {...rest}
      >
        {
          (
            {
              [FieldType.Input]: <Input {...params} />,
              [FieldType.Checkbox]: <Checkbox {...params} />,
              [FieldType.Radio]: <Radio {...params} />,
              [FieldType.Phone]: <PhoneMask {...params} />,
              [FieldType.DriverLicense]: <DriverLicenseMask {...params} />,
            } as {
              [key in FieldType]?: JSX.Element;
            }
          )[type]
        }
      </Form.Item>
    </Form.Item>
  </Field>
);

export default FormField;
