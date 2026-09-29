import React, { FC } from 'react';
import { observer } from 'mobx-react';
import { Button } from 'antd';

import { EmptyDataList } from 'shared/components/EmptyFactory/EmptyFactory';
import { DelegatesTexts, DelegatesTextsCyrillic } from '../../Delegates.constants';
import { useModal } from '../../context/modal.context';
import styles from './EmptyDelegates.module.scss';

const EmptyDelegates: FC = observer(() => {
  const { openAdd } = useModal();

  return (
    <div className={styles.emptyWrapper}>
      <div className={styles.emptyContent}>
        <EmptyDataList
          title="Делегаты еще не назначены"
          message="Вы можете делегировать полномочия по согласованию заявок"
        />
        <Button
          type="primary"
          size="middle"
          className={styles.addButton}
          onClick={openAdd}
        >
          {DelegatesTextsCyrillic[DelegatesTexts.add]}
        </Button>
      </div>
    </div>
  );
});

export default EmptyDelegates;
