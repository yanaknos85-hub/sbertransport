import { FC } from 'react';
import { Checkbox as AntdCheckbox, CheckboxProps } from 'antd-mobile';
import cn from 'classnames';

import styles from './Checkbox.module.scss';

const Checkbox: FC<CheckboxProps> = ({
  value, className, children, ...rest
}) => (
  <AntdCheckbox className={cn(styles.checkbox, [className])} {...rest}>
    {children}
  </AntdCheckbox>
);

export default Checkbox;
