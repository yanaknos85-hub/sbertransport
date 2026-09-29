import { Form, Radio } from 'antd';
import React, { FC } from 'react';
import styles from '../tripDetailView.module.scss';

interface Props {
  options: { label: string; value: string }[];
}

export const CancelCodes: FC<Props> = ({ options }) => (
  <Form.Item name="cancelCode">
    <Radio.Group
      size="large"
      className={styles.radioGroup}
      options={options}
    />
  </Form.Item>
);
