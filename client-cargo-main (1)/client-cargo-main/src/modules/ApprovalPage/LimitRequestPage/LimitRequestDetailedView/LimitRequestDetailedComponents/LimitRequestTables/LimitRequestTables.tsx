import React, { FC } from 'react';
import { observer } from 'mobx-react';

import { LimitRequestInfo } from 'stores/Limits/Limit.interface';

import { ApproversDataTable } from './ApproversDataTable';
import { LimitCostsTable } from './LimitCostsTable';
import { LimitTransfersTable } from './LimitTransfersTable';

import styles from '../../styles.module.scss';

export const LimitRequestTables: FC<{
  request: LimitRequestInfo;
}> = observer(({ request }) => (
  <>
    {request.limitId && (
      <>
        <div style={{ overflowX: 'auto' }}>
          <span className={styles.infoBold}>Корректировки по лимиту</span>
          <LimitTransfersTable request={request} />
        </div>

        <div>
          <span className={styles.infoBold}>Расходы по лимиту</span>
          <LimitCostsTable request={request} />
        </div>
      </>
    )}

    {request.askTargets === 'SIBLINGS' && (
      <div>
        <span className={styles.infoBold}>
          Таблица с информацией о результатах рассмотрения заявки в смежных подразделениях
        </span>
        <ApproversDataTable request={request} />
      </div>
    )}
  </>
));
