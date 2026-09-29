# Юзается так

Передаются любые параметры в форму элемента анта какие указываются у них в доках
[https://ant.design/components/form/]

По сути это просто обертка над элементами формы анта, стилистически настроенных
и обернутыми Form.Item

```tsx
import React, { FC } from 'react';
import { Form } from 'antd';
import moment from 'moment';
import { DATE_FORMAT } from 'constants/constants.app';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import Button from 'shared/form/Button/Button';
import { ValidationRules } from 'shared/fieldValidationRules';

const AnyComponent: FC<{ data: any }> = ({ data }) => {
  const [form] = Form.useForm();
  const { required } = ValidationRules.general;

  const field = {
    senderAddress: {
      label: 'Адрес отправителя',
      name: 'senderAddress',
      initialValue: 'Какой то адрес',
      type: FieldType.address,
      rules: [required],
    },
    senderName: {
      label: 'ФИО отправителя',
      name: 'senderName',
      initialValue: 'Иванов Иван',
      rules: [required],
    },
    senderPhone: {
      label: 'Телефон',
      name: 'senderPhone',
      initialValue: '+ 7 926 123 23 32',
      type: FieldType.phone,
      rules: [required],
    },
    sourceLoaders: {
      label: 'Требуется грузчик в точке отправления',
      name: 'sourceLoaders',
      initialValue: true,
      type: FieldType.checkbox,
    },
    tariff: {
      type: FieldType.select,
      label: 'Тариф',
      name: 'tariff',
      initialValue: 1,
      params: {
        options: [
          {label: 'tariff 1', value: 1},
          {label: 'tariff 2', value: 2},
          {label: 'tariff 3', value: 3},
        ],
      }
    },
    desiredDate: {
      type: FieldType.date,
      label: 'Дата отправления',
      name: 'desiredDate',
      initialValue: 1736932107927,
      rules: [required],
      params: {
        format: DATE_FORMAT.DATE_WITH_TIME,
      }
    },
  }

  const onSave = async () => {
    try {
      const values = await form.validateFields();
      console.log(values);
    } catch (err) {
      const error = err as any;
      console.log('Failed:', error);
    }


  return (
    <Form form={form}>
      <FormField {...field.senderAddress} />
      <FormField {...field.senderName} />
      <FormField {...field.senderPhone} />
      <FormField {...field.sourceLoaders} />
      <FormField {...field.tariff} />
      <FormField {...field.desiredDate} />

      <Button onClick={onSave}>Сохранить</Button>
    </Form>
  )
}
```
