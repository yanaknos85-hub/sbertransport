import React from 'react';
import classNames from 'classnames';
import { Input as AntDesignInput } from 'antd';
import styles from './input.module.scss';
import { SearchProps } from 'antd/lib/input';

const Input = ({ noBorder = true, ...props }) => (
  <AntDesignInput className={classNames(styles.input, props.className, { [styles.noBorder]: noBorder })} {...props} />
);

Input.Search = (props: SearchProps) => (
  <AntDesignInput.Search className={classNames(styles.input, styles.search, props.className)} {...props} />
);

export default Input;
