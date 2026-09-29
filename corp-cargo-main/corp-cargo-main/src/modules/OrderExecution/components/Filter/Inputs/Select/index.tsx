import React from 'react';
import classNames from 'classnames';
import { Select as AntDesignSelect } from 'antd';
import DownArrowIcon from './assets/DownArrowIcon';
import styles from './select.module.scss';

const Select = ({ ...props }) => {
  return (
    <AntDesignSelect
      suffixIcon={DownArrowIcon}
      className={classNames(styles.select, props.className)}
      {...props}
    />
  );
};
export default Select;
