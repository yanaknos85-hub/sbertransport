import React from 'react';
import classNames from 'classnames';
import { Input as AntDesignInput } from 'antd';
import styles from './input.module.scss';

const Input = ({ ...props }) => {
  return <AntDesignInput className={classNames(styles.input, props.className)} {...props} />;
};
export default Input;
