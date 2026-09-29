import React from 'react';
import classNames from 'classnames';
import { Select as AntDesignSelect } from 'antd';
import styles from './select.module.scss';
import DownArrowIcon from './assets/DownArrowIcon';

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
