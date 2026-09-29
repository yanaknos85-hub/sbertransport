import React from 'react';
import classNames from 'classnames';
import { Input as AntDesignInput } from 'antd';
import styles from './input.module.scss';

const Input = ({ noBorder = true, ...props }) => (
  <AntDesignInput className={classNames(styles.input, props.className, { [styles.noBorder]: noBorder })} {...props} />
);

export default Input;
