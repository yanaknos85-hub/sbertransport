import React, { FC } from 'react';
import classNames from 'classnames';
import { Props } from 'shared/components/SelectDepartment';
import styles from './select.module.scss';

import { SelectEmployee } from './SelectEmployee';
import DownArrowIcon from 'shared/assets/DownArrowIcon';

export const SelectEmployeeFilter: FC<Props> = ({ ...props }) => (
  <SelectEmployee
    suffixIcon={DownArrowIcon}
    className={classNames(styles.select, props.className)}
    {...props}
  />
);
