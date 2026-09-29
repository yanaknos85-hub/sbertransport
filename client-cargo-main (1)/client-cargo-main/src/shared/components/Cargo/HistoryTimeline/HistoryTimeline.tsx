import React from 'react';
import { CheckOutlined } from '@ant-design/icons';
import { Timeline } from 'antd';
import moment from 'moment';
import Question from 'shared/components/Images/view/menu 2.0/Question';

import { CargoHistoryType, CargoHistoryTypeEnum } from 'stores/Cargos/types';
import { CargoRequestStatusesTitles, FINAL_STATUSES } from 'constants/CargoRequestStatuses.constants';
import { DATE_FORMAT } from 'constants/constants.app';

import styles from './HistoryTimeline.module.scss';

const getIcon = (type: CargoHistoryTypeEnum): JSX.Element => {
  switch (type) {
    case CargoHistoryTypeEnum.FUTURE:
      return <div className={styles.futureIcon} />;
    case CargoHistoryTypeEnum.NOW:
      return (
        <div className={styles.nowIcon}>
          <div className={styles.nowInnerCircle} />
        </div>
      );
    case CargoHistoryTypeEnum.PAST:
      return (
        <div className={styles.pastIcon}>
          <CheckOutlined color="#fff" size={8} />
        </div>
      );
    default:
      return (
        <div className={styles.futureIcon}>
          <Question />
        </div>
      );
  }
};

const getColor = (type: CargoHistoryTypeEnum): string => {
  switch (type) {
    case CargoHistoryTypeEnum.FUTURE:
      return '#D6D6D6';
    case CargoHistoryTypeEnum.NOW:
      return '#FF9A32';
    case CargoHistoryTypeEnum.PAST:
      return '#10BF6A';
    default:
      return '#fff';
  }
};

const HistoryTimeline = ({
  cargoHistoryType,
  isLast,
}: {
  cargoHistoryType: CargoHistoryType;
  isLast: boolean;
}): JSX.Element => {
  const effectiveType = FINAL_STATUSES.has(cargoHistoryType.status)
    ? CargoHistoryTypeEnum.PAST
    : cargoHistoryType.type;

  return (
    <Timeline.Item
      className={`${styles.timelineItem} ${isLast ? styles.timelineItemLast : ''}`}
      key={cargoHistoryType.status}
      color={getColor(effectiveType)}
      dot={getIcon(effectiveType)}
    >
      <div title={cargoHistoryType.status}>
        <span className={styles.status}>
          {cargoHistoryType?.status && CargoRequestStatusesTitles[cargoHistoryType?.status]}
        </span>
        <span className={styles.date}>
          {moment(cargoHistoryType.date).format(`${DATE_FORMAT.BASE_REVERTED_DOTS} ${DATE_FORMAT.TIME_FULL}`)}
        </span>
      </div>
    </Timeline.Item>
  );
};

export default HistoryTimeline;
