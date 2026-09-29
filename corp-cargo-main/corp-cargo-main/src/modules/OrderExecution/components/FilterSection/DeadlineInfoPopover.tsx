import React, { FC } from 'react';
import { Popover } from 'antd';
import { InfoCircleOutlined } from '@ant-design/icons';
import DeadlineCircleIcon from './DeadlineCircleIcon';
import { DeadlineType } from './deadline.constants';

import styles from './styles.module.scss';

const infoContent = (
  <div className="deadlineInfo">
    <div className="infoSection">
      <strong>
        <span className={styles.infoCircleIcon}><DeadlineCircleIcon type={DeadlineType.LESS_THAN_20} /></span>
        Скоро истекает / Нарушен (&lt;20%)
      </strong>
    </div>
    <div className="infoSection">
      <strong>
        <span className={styles.infoCircleIcon}><DeadlineCircleIcon type={DeadlineType.BETWEEN_20_AND_50} /></span>
        Есть запас времени (20–50%)
      </strong>
    </div>
    <div className="infoSection">
      <strong>
        <span className={styles.infoCircleIcon}><DeadlineCircleIcon type={DeadlineType.MORE_THAN_50} /></span>
        Достаточно времени (&gt;50%)
      </strong>
    </div>
    <br />
    <div className="infoSection">
      <p>Фильтр показывает, сколько процентов времени осталось до контрольного срока.<br />Чем меньше процент — тем ближе дедлайн.</p>
    </div>
  </div>
);

const DeadlineInfoPopover: FC = () => (
  <div className={styles.suffixContent}>
    <Popover content={infoContent} placement="right" mouseEnterDelay={0.3}>
      <InfoCircleOutlined className={styles.infoIcon} />
    </Popover>
  </div>
);

export default DeadlineInfoPopover;