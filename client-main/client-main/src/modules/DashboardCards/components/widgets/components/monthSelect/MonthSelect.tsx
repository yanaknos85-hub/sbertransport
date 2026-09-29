import React, { FC } from 'react';
import { Select } from 'antd';
import moment from 'moment/moment';

import './styles.scss';

const { Option } = Select;

interface TAddFavoriteModalProps {
  onChange: (date: moment.Moment) => void;
}

const MonthSelect: FC<TAddFavoriteModalProps> = ({ onChange }) => {
  // Генерация списка месяцев от текущего минус 12 месяцев
  const generateMonths = () => {
    const months = [];

    for (let i = 0; i < 12; i++) {
      const month = moment().subtract(i, 'months');
      months.push(month);
    }

    return months;
  };

  const months = generateMonths();

  return (
    <Select
      placeholder="Выберите месяц"
      onChange={value => onChange(moment(value))}
      defaultValue={moment().format('MMMM')}
      className="monthSelect"
    >
      {months.map(month => (
        <Option
          key={month.format('YYYY-MM')}
          value={month.toISOString()}
        >
          {month.format('MMMM')}
        </Option>
      ))}
    </Select>
  );
};

export default MonthSelect;
