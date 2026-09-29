import React, { FC, useState } from 'react';

import { CheckboxChangeEvent } from 'antd/lib/checkbox';
import cn from 'classnames';

import Checkbox from 'components/Checkbox/Checkbox';
import Flex from 'components/Flex/Flex';

import styles from './DayOfWeekPicker.module.scss';

export enum DaysOfWeek {
  Mo, Tu, We, Th, Fr, Sa, Su,
}

const days: Record<DaysOfWeek, string> = {
  [DaysOfWeek.Mo]: 'Пн',
  [DaysOfWeek.Tu]: 'Вт',
  [DaysOfWeek.We]: 'Ср',
  [DaysOfWeek.Th]: 'Чт',
  [DaysOfWeek.Fr]: 'Пт',
  [DaysOfWeek.Sa]: 'Сб',
  [DaysOfWeek.Su]: 'Вс',
};

interface DayOfWeekPickerProps {
  value?: Record<DaysOfWeek, boolean>;
  onChange?: (days: DayOfWeekPickerProps['value']) => void;
}

export const DayOfWeekPicker: FC<DayOfWeekPickerProps> = ({
  value,
  onChange,
}) => {
  const [onlyWorkingDays, setOnlyWorkingDays] = useState(false);

  const toggleDay = (day: DaysOfWeek) => () => {
    onChange?.({
      [DaysOfWeek.Mo]: false,
      [DaysOfWeek.Tu]: false,
      [DaysOfWeek.We]: false,
      [DaysOfWeek.Th]: false,
      [DaysOfWeek.Fr]: false,
      [DaysOfWeek.Sa]: false,
      [DaysOfWeek.Su]: false,
      ...value,
      [day]: !value?.[day],
    });
  };

  const selectWorkingDays = (e: CheckboxChangeEvent) => {
    setOnlyWorkingDays(e.target.checked);

    onChange?.({
      [DaysOfWeek.Mo]: true,
      [DaysOfWeek.Tu]: true,
      [DaysOfWeek.We]: true,
      [DaysOfWeek.Th]: true,
      [DaysOfWeek.Fr]: true,
      [DaysOfWeek.Sa]: false,
      [DaysOfWeek.Su]: false,
    });
  };

  return (
    <div>
      <Flex gap={10} marginBottom>
        {Object.entries(days).map(([day, title]) => (
          <button
            key={day}
            onClick={toggleDay(+day)}
            className={cn(styles.day, { [styles.active]: !!value?.[+day as DaysOfWeek] })}
            disabled={onlyWorkingDays}
            type="button"
          >
            {title}
          </button>
        ))}
      </Flex>

      <Checkbox
        checked={onlyWorkingDays}
        onChange={selectWorkingDays}
      >
        Только рабочие дни
      </Checkbox>
    </div>
  );
};
