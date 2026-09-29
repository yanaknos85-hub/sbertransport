import React, { type FC } from 'react';
import { Progress } from 'antd';
import { formatRublesWithoutRemainder, toRubles } from 'utils';
import { PROGRESS_FILL_COLOR, PROGRESS_STROKE_COLOR } from '../../../constants';
import styles from './limitsView.module.scss';

interface LimitsViewProps {
  sum?: number;
  rest?: number;
  cost: number;
  isWithLimitsPage: boolean;
}

export const LimitsView: FC<LimitsViewProps> = ({
  sum, rest, cost, isWithLimitsPage,
}) => {
  const restPercentage = rest && sum && Math.round(rest * 100 / sum);

  return (
    <div className={styles.limitsContainer}>
      <div className={styles.limitsHeader}>
        <span className={styles.limitsHeaderTitle}>Предварительная стоимость</span>
        <span className={styles.limitsHeaderCost}>{formatRublesWithoutRemainder(cost)}</span>
      </div>
      {isWithLimitsPage && rest && sum
      && (
      <>
        <Progress
          percent={restPercentage}
          showInfo={false}
          trailColor={PROGRESS_FILL_COLOR}
          strokeColor={PROGRESS_STROKE_COLOR}
        />
        <div className={styles.limitsFooter}>
          <span>Остаток лимита подразделения</span>
          {' '}
          <span>
            {formatRublesWithoutRemainder(toRubles(rest))}
            {' '}
            из
            {' '}
            {formatRublesWithoutRemainder(toRubles(sum))}
          </span>
        </div>
      </>
      )}
    </div>
  );
};
