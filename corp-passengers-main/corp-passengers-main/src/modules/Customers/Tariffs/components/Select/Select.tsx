import React, { type FC } from 'react';
import { Select as AntdSelect, type SelectProps } from 'antd';
import { SelectValue } from 'antd/lib/select';
import cn from 'classnames';

import styles from './select.module.scss';

interface Props<T extends SelectValue = string> extends SelectProps<T> {
  className?: string;
}

export const Select: FC<Props> = ({ className, ...props }) => {
  return (
    <AntdSelect className={cn(styles.select, className)} {...props} />
  );
};
