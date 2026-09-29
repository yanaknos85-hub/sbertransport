import React from 'react';
import { Progress } from 'antd';
import { formatRublesWithoutRemainder, toRubles } from 'utils';
import { YANDEX_LIMITS_PROGRESS_FILL_COLOR, YANDEX_LIMITS_PROGRESS_STROKE_COLOR } from '../../constants/yandexTaxi.constants';

import styles from './ApprovalYandexLimitView.module.scss';

interface ApprovalYandexLimitViewProps {
  rest: number;
  sum: number;
  cost: number;
  plannedCost: number;
}

export const ApprovalYandexLimitView = ({
  rest, sum, cost, plannedCost,
}: ApprovalYandexLimitViewProps) => {
  const restPercentage = Math.round(rest * 100 / sum);

  return (
    <div className={styles.limitsContainer}>
      <div className={styles.plannedCost}>
        <span className={styles.plannedCostTitle}>Предварительная стоимость</span>
        <span className={styles.plannedCostPrice}>{formatRublesWithoutRemainder(plannedCost)}</span>
      </div>

      <div className={styles.limitsHeader}>
        <span className={styles.limitsHeaderTitle}>Cтоимость</span>
        <span className={styles.limitsHeaderCost}>{formatRublesWithoutRemainder(cost)}</span>
      </div>
      <Progress
        percent={restPercentage}
        showInfo={false}
        trailColor={YANDEX_LIMITS_PROGRESS_FILL_COLOR}
        strokeColor={YANDEX_LIMITS_PROGRESS_STROKE_COLOR}
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
    </div>
  );
};
