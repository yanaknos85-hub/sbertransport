import React from 'react';
import { SelectProps, SelectValue } from 'antd/lib/select';
import { Select as SelectAnt, Spin } from 'antd';
import cn from 'classnames';
import styles from './Select.module.scss';

import { ReactComponent as Icon } from './images/selectArrow.svg';

type SelectType = SelectProps<SelectValue> & { className?: string };

const Select = (props: SelectType) => {
  const { className } = props;
  return (
    <SelectAnt
      className={cn(styles.select, className)}
      suffixIcon={props.loading ? <Spin size="small" /> : <Icon />}
      {...props}
    />
  );
};

export default Select;
