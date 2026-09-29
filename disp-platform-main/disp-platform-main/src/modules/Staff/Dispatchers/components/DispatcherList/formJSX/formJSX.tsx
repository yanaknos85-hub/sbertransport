import React, { useEffect } from 'react';
import { Form, Input, Select } from 'antd';
import { FormInstance } from 'antd/lib/form';
import { Translation } from 'i18n';

import { resetFormFields } from 'components/List/lib/handlers';
import { statuses } from 'modules/Staff/Dispatchers/constants/statuses';
import styles from './formJSX.module.scss';

export const formJSX
  = (t: Translation) => (
    form: FormInstance,
    initialValues: Record<string, unknown>,
    onFinish: (values: Record<string, unknown>) => void
  ): JSX.Element => {
    useEffect(() => {
      resetFormFields(form);
      form.setFieldsValue(initialValues);
    }, [initialValues, form]);

    return (
      <Form
        className={styles.filtersForm}
        form={form}
        initialValues={initialValues}
        onFinish={onFinish}
      >
        <Form.Item
          className={styles.formItem}
          name="lastName"
          label={t.Registry.Labels.lastName}
        >
          <Input className={styles.input} />
        </Form.Item>

        <Form.Item
          className={styles.formItem}
          name="firstName"
          label={t.Registry.Labels.firstName}
        >
          <Input className={styles.input} />
        </Form.Item>

        <Form.Item
          className={styles.formItem}
          name="patronymic"
          label={t.Registry.Labels.patronymic}
        >
          <Input className={styles.input} />
        </Form.Item>

        <Form.Item
          className={styles.formItem}
          name="status"
          label={t.Registry.Labels.status}
        >
          <Select
            disabled
            className={styles.select}
            options={statuses}
          />
        </Form.Item>
      </Form>
    );
  };
