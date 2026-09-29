import React, { FC, useMemo } from 'react';
import { useTranslation } from 'i18n';
import moment, { Moment } from 'moment';
import cn from 'classnames';

import {
  DateRange,
  DateRangeISO
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { DATE_FORMAT } from 'constants/constants.app';
import styles from './DisplayPeriod.module.scss';

interface Props {
  className?: string;
  period: (DateRange | DateRangeISO)[];
}

const formatPeriod = (date: string | Moment) => moment(date).format(DATE_FORMAT.BASE_REVERTED_DOTS);

export const DisplayPeriod: FC<Props> = ({ className, period }) => {
  const { t } = useTranslation();

  const dateRange = useMemo(() => {
    if (period.length) {
      const { start, end } = period[0];

      return `${formatPeriod(start)}-${formatPeriod(end)}`;
    }

    return null;
  }, [period]);

  return (
    <div className={cn(className, styles.container)}>
      <span className={styles.label}>{t.global.periodOfRegistryDisplay}</span>
      <span className={styles.period}>{dateRange ?? 'не выбран'}</span>
    </div>
  );
};
