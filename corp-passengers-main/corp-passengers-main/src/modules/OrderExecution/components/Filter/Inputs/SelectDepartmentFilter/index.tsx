import React, { FC } from 'react';
import classNames from 'classnames';
import { Props, SelectDepartment } from 'shared/components/SelectDepartment';
import styles from './select.module.scss';
import DownArrowIcon from './assets/DownArrowIcon';

export const SelectDepartmentFilter: FC<Props> = ({ ...props }) => (
  <SelectDepartment
    suffixIcon={DownArrowIcon}
    className={classNames(styles.select, props.className)}
    {...props}
  />
);
