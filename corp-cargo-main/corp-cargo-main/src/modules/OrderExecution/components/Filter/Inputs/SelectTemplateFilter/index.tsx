import React from 'react';
import classNames from 'classnames';
import { Select as AntDesignSelect } from 'antd';
import DownArrowIcon from './assets/DownArrowIcon';
import styles from '../Select/select.module.scss'

export const SelectTemplateFilter = ({ options, ...props }) => {
  return (
    <AntDesignSelect
      options={options}
      suffixIcon={DownArrowIcon}
      className={classNames(styles.select, props.className)}
      {...props}
    />
  );
};
