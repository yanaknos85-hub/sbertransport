import React, { FC } from 'react';
import { Select as AntdSelect } from 'antd';
import { OptionProps, SelectProps, SelectValue } from 'antd/lib/select';
import styles from './index.module.scss';
import cn from 'classnames';

export const Select = <T extends SelectValue = SelectValue>({ className, ...props }: SelectProps<T>) => (
  <AntdSelect<T> className={cn(styles.select, className)} {...props} />
);

export const Option: FC<OptionProps> = props => <AntdSelect.Option {...props} />;
