import React, { useEffect } from 'react';
import { Form, Input } from 'antd';
import { FormInstance } from 'antd/lib/form';
import { Translation } from 'i18n';

import { getEmptyFieldValues } from 'utils/form';
import styles from './formJSX.module.scss';

export const formJSX
  = (t: Translation) => (
    form: FormInstance,
    initialValues: Record<string, unknown>,
    onFinish: (values: Record<string, unknown>) => void
  ): JSX.Element => {
    useEffect(() => {
      const emptyFieldValues = getEmptyFieldValues(form);

      form.resetFields();
      form.setFieldsValue({ ...emptyFieldValues, ...initialValues });
    }, [form, initialValues]);

    return (
      <Form
        className={styles.filtersForm}
        form={form}
        initialValues={initialValues}
        onFinish={onFinish}
      >
        <Form.Item
          className={styles.formItem}
          name="name"
          label={t.Autoparks.Labels.name}
        >
          <Input className={styles.input} placeholder={t.AutoparkTable.searchPlaceholder} />
        </Form.Item>
      </Form>
    );
  };
