import React, { useState } from 'react';
import cn from 'classnames';

import { monthNames } from 'utils/calendar';
import { ReactComponent as ArrowLeftIcon } from 'assets/icons/arrow-left.svg';
import { ReactComponent as ArrowRightIcon } from 'assets/icons/arrow-right.svg';

import styles from './SelectMonth.module.scss';

interface SelectMonthProps {
  value: number;
  onChange: (month: number) => void;
  className?: string;
}

const SelectMonth: React.FC<SelectMonthProps> = ({
  value, onChange, className,
}) => {
  const [month, setMonth] = useState(value);

  const onPrevMonth = () => {
    const newMonth = month === 0 ? 11 : month - 1;
    setMonth(newMonth);
    onChange(newMonth);
  };

  const onNextMonth = () => {
    const newMonth = month === 11 ? 0 : month + 1;
    setMonth(newMonth);
    onChange(newMonth);
  };

  return (
    <div className={cn(styles.container, className)}>
      <button onClick={onPrevMonth}>
        <ArrowLeftIcon />
      </button>
      <span>{monthNames[month]}</span>
      <button onClick={onNextMonth}>
        <ArrowRightIcon />
      </button>
    </div>
  );
};

export default SelectMonth;
