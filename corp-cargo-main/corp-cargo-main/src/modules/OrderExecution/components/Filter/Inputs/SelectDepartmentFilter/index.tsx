import React, { FC } from 'react';
import classNames from 'classnames';
import { Props, SelectDepartment } from 'modules/OrderExecution/components/SelectDepartment/SelectDepartment';
import DownArrowIcon from './assets/DownArrowIcon';
import styles from './select.module.scss';

export const SelectDepartmentFilter: FC<Props> = ({ ...props }) => (
  <SelectDepartment
    suffixIcon={DownArrowIcon}
    className={classNames(styles.select, props.className)}
    {...props}
  />
);
