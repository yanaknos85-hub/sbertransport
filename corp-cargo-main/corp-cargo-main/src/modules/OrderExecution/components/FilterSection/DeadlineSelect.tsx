import React, { FC } from 'react';
import type { SelectValue } from 'antd/lib/select';
import classNames from 'classnames';

import DeadlineCircleIcon from './DeadlineCircleIcon';
import DeadlineInfoPopover from './DeadlineInfoPopover';
import { DeadlineType, getSelectWidth } from './deadline.constants';
import type { DeadlineValue } from './deadline.constants';
import Select from '../Select/Select';

import styles from './styles.module.scss';

interface Props {
  value?: string;
  hasValue: boolean;
  onChange: (value: SelectValue) => void;
}

const DEADLINE_OPTIONS = [
  {
    label: (
      <span className={styles.optionLabel}>
        <span className={styles.circleIcon}><DeadlineCircleIcon type={DeadlineType.LESS_THAN_20} /></span>
        <span>Скоро истекает</span>
      </span>
    ),
    value: DeadlineType.LESS_THAN_20,
  },
  {
    label: (
      <span className={styles.optionLabel}>
        <span className={styles.circleIcon}><DeadlineCircleIcon type={DeadlineType.BETWEEN_20_AND_50} /></span>
        <span>Есть запас времени</span>
      </span>
    ),
    value: DeadlineType.BETWEEN_20_AND_50,
  },
  {
    label: (
      <span className={styles.optionLabel}>
        <span className={styles.circleIcon}><DeadlineCircleIcon type={DeadlineType.MORE_THAN_50} /></span>
        <span>Достаточно времени</span>
      </span>
    ),
    value: DeadlineType.MORE_THAN_50,
  },
];

const DeadlineSelect: FC<Props> = ({ value, hasValue, onChange }) => (
  <Select
    style={{ width: getSelectWidth(value as DeadlineValue | null | undefined) }}
    className={classNames(styles.select, { [styles.hasValue]: hasValue })}
    options={DEADLINE_OPTIONS}
    allowClear
    placeholder="Остаток до КС (%)"
    value={value}
    onChange={onChange}
    suffixIcon={<DeadlineInfoPopover />}
  />
);

export default DeadlineSelect;