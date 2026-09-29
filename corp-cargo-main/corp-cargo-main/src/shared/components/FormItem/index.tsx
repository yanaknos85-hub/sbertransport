import Form, { FormItemProps } from 'antd/lib/form';
import React, { FC } from 'react';
import cn from 'classnames';
import styles from './index.module.scss';

export const FormItem: FC<FormItemProps> = ({ className, ...props }) => (
  <Form.Item className={cn(styles.formItem, className)} {...props} />
);
