import React, { FC } from 'react';
import cn from 'classnames';
import styles from './index.module.scss';
import { Form, FormItemProps } from '@sber-sbertransport/ui-kit/src';

export const FormItem: FC<FormItemProps> = ({ className, ...props }) => (
  <Form.Item className={cn(styles.formItem, className)} {...props} />
);
