import { Select as AntdSelect } from 'antd';
import React, { FC } from 'react';
import cn from 'classnames';
import { OptionProps } from 'antd/lib/select';
import { ReactComponent as DownArrowIcon } from 'shared/assets/svg/down-arrow.svg';
import styles from './select.module.scss';

export const Select: FC<React.ComponentProps<typeof AntdSelect>> = props => (
  <AntdSelect
    suffixIcon={<DownArrowIcon />}
    {...props}
    className={cn(styles.select, props.className)}
  />
);

export const Option: FC<OptionProps> = props => <AntdSelect.Option {...props} />;
