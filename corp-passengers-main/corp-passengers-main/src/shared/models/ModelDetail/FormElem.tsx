import { List } from 'antd';
import React, { FC } from 'react';
import { ModelFormFieldProps } from 'shared/models/ModelDetail/ModelFormField';
import styles from './modelDetail.module.scss';

interface IFormElem {
  fieldProps?: ModelFormFieldProps;
}

export const FormElem: FC<IFormElem> = ({ fieldProps, children }) => (
  <List.Item>
    {fieldProps?.header && (
      <h3 className={`${styles.header} ${fieldProps.noHeaderPadding ? styles.no_header_padding : ''}`}>
        {fieldProps.header}
      </h3>
    )}

    {children}
  </List.Item>
);
