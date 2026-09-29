import React, { FC, useEffect, useState } from 'react';
import { useTranslation } from 'i18n';

import { Button, Form } from 'antd';
import { FormInstance } from 'antd/lib/form/Form';
import { MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';

import { NumericInput } from 'shared/components/Inputs/NumericInput';
import { ModelFormField, ModelFormFieldType, ModelFormFieldProps } from 'shared/models/ModelDetail/ModelFormField';
import uniqId from 'utils/uid';

import { useFields } from './useFields';

import styles from './styles.module.scss';

const getDateRequiredState = (form?: FormInstance): boolean => {
  const fields = form?.getFieldsValue();
  // If date start or end selected return false
  if (fields?.pickupStartTime || fields?.dropStartTime) {
    return false;
  }
  // Default case
  return true;
};

const IntermediateAddressField: FC<{
  form: FormInstance;
  removeIntermediateAddress: React.Dispatch<string>;
  name: string;
}> = ({
  form, name, removeIntermediateAddress,
}) => {
  const { t } = useTranslation();
  return (
    <div className={styles.addressDynamic}>
      <ModelFormField
        form={form}
        name={`address--${name}`}
        fieldType={ModelFormFieldType.ADDRESS_AUTO_COMPLETE}
        description={t.Forms.sharedRidesSearch.address}
        className={styles.addressAutocomplete}
        editable
        required
      />
      <Form.Item name={`address--${name}-waitTime`} className={styles.waitTime}>
        <NumericInput suffix={<small>мин</small>} />
      </Form.Item>
      <Button
        type="primary"
        icon={<MinusCircleOutlined />}
        size="middle"
        onClick={() => removeIntermediateAddress(name)}
      />
    </div>
  );
};

export const useFilterFields: () => ModelFormFieldProps[] = () => [
  {
    fieldType: ModelFormFieldType.CUSTOM,
    editable: true,
    name: 'layout',
    component: ({ form }) => <FilterFormLayout form={form} />,
  },
];

const FilterFormLayout = ({ form }: { form: FormInstance }) => {
  const { t } = useTranslation();
  const [dateIsRequired, setDateIsRequired] = useState(true);

  const [intermediateAddressFields, setIntermediateAddressFields] = useState<ModelFormFieldProps[]>([]);

  const removeIntermediateAddress = (name: string): void => {
    setIntermediateAddressFields(fields => fields.filter(filter => filter.name !== name));
  };

  const addIntermediateAddress = () => {
    const name = uniqId('addr-');

    const newField: ModelFormFieldProps = {
      fieldType: ModelFormFieldType.CUSTOM,
      name,
      editable: true,
      component: ({ form }) => (
        <IntermediateAddressField
          form={form}
          removeIntermediateAddress={removeIntermediateAddress}
          name={name}
        />
      ),
    };

    setIntermediateAddressFields([...intermediateAddressFields, newField]);
  };

  useEffect(() => {
    // Revalidate date on change dateIsRequired flag state
    if (form?.isFieldsTouched()) {
      form.validateFields(['pickupStartTime', 'dropStartTime']);
    }
  }, [form, dateIsRequired]);

  const filterFields: ModelFormFieldProps[] = [
    ...useFields().filter(field => field.editable),
    {
      name: 'pickupStartTime',
      fieldType: ModelFormFieldType.DATE,
      description: t.Forms.sharedRidesSearch.pickupStartTime,
      editable: true,
      rules: [{ required: dateIsRequired, message: 'Введите дату отправления или прибытия' }], // TODO: i18n
      onChange: () => setDateIsRequired(getDateRequiredState(form)),
    },
    {
      name: 'dropStartTime',
      fieldType: ModelFormFieldType.DATE,
      description: t.Forms.sharedRidesSearch.dropStartTime,
      editable: true,
      rules: [{ required: dateIsRequired, message: 'Введите дату отправления или прибытия' }], // TODO: i18n
      onChange: () => setDateIsRequired(getDateRequiredState(form)),
    },
    {
      name: 'addressStart',
      fieldType: ModelFormFieldType.ADDRESS_AUTO_COMPLETE,
      description: t.Forms.sharedRidesSearch.addressStart,
      editable: true,
      required: true,
    },
    ...intermediateAddressFields,
    {
      name: 'addressEnd',
      fieldType: ModelFormFieldType.ADDRESS_AUTO_COMPLETE,
      description: t.Forms.sharedRidesSearch.addressEnd,
      editable: true,
      required: true,
    },
    {
      name: 'custom',
      fieldType: ModelFormFieldType.CUSTOM,
      editable: false,
      component: () => (
        <Button
          icon={<PlusOutlined />}
          size="middle"
          onClick={addIntermediateAddress}
          className={styles.buttonAdd}
        >
          {t.Forms.sharedRidesSearch.buttonAddAddress}
        </Button>
      ),
    },
  ];

  return (
    <>
      {filterFields.map(field => (
        <ModelFormField form={form} {...field} />
      ))}
    </>
  );
};
