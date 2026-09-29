import React, { FC } from 'react';
import cn from 'classnames';
import { Tooltip } from 'antd';

import { useTripsQuery } from 'modules/Trips/context/TripsQuery';
import { EXPECTED_TIME } from 'modules/Trips/constants';

import { ReactComponent as QuestionMark } from 'assets/icons/question.svg';
import styles from './DurationFilter.module.scss';

export const DurationFilter: FC = () => {
  const { query, setQuery } = useTripsQuery();

  const onApplyFilter = () => {
    setQuery({
      ...query,
      expectedTime: !query.expectedTime ? EXPECTED_TIME : undefined,
    });
  };

  return (
    <div
      className={cn(styles.duration, {
        [styles.activeDuration]: query.expectedTime,
      })}
      onClick={onApplyFilter}
    >
      <span>Длительные</span>
      <Tooltip title="Поездки более 6 часов">
        <QuestionMark />
      </Tooltip>
    </div>
  );
};
