import React, { FC } from 'react';
import { Checkbox } from 'antd';
import styles from './switchComponents.module.scss';

interface Props {
  checked: boolean;
  onCheckChange: React.Dispatch<boolean>;
  label: string;
  checkBoxLabel: string;
}
export const LabelInterval: FC<Props> = ({
  checked, onCheckChange, label, checkBoxLabel,
}) => (
  <div className={styles.label}>
    <span>
      {label}
      :
    </span>
    <div>
      <Checkbox
        onChange={e => onCheckChange(e.target.value)}
        checked={checked}
        className={styles.checkbox}
      />
      <span>{checkBoxLabel}</span>
    </div>
  </div>
);
