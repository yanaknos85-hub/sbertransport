import React, { FC } from 'react';
import { Tooltip } from 'antd';
import { QuestionCircleFilled } from '@ant-design/icons';
import { useTranslation } from 'i18n';

import styles from './styles.module.scss';

// заглушка, пока нет бэка
export const ControlTime: FC = () => {
  const { t } = useTranslation();
  return (
    <div className={styles.controlTime}>
      <div className={styles.controlTimeHeader}>
        {t.SettingsTripRules.sharedRides.controlTimeTitle}
        <Tooltip title={t.SettingsTripRules.sharedRides.controlTimeToolTip}>
          <QuestionCircleFilled style={{
            color: '#ccc', fontSize: 16, paddingLeft: 4,
          }}
          />
        </Tooltip>
      </div>

      <div className={styles.controlTimeInput}>
        <div>60</div>
      </div>
    </div>
  );
};
