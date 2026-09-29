import React from 'react';
import { Form } from 'antd';
import styles from './formItem.module.scss';

// @ts-ignore
const FormItem = ({ children, ...props }) => {
  return (<Form.Item className={styles.formItem} {...props}>{children}</Form.Item>);
};

export default FormItem;
