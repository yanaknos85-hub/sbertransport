import React, { FC, useEffect } from 'react';
import { observer } from 'mobx-react';
import { Button, Table } from 'antd';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { Pagination } from 'shared/components/Pagination/Pagination';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';
import { usePagination } from 'shared/hooks/usePagination';
import { StoreNames } from 'stores/StoreNames.enum';

import EmptyDelegates from './components/EmptyDelegates/EmptyDelegates';
import DelegateAddEditModal from './components/DelegateAddEditModal/DelegateAddEditModal';
import DelegateDeleteModal from './components/DelegateDeleteModal/DelegateDeleteModal';
import DelegateCard from './components/DelegateCard/DelegateCard';
import { DelegatesTexts, DelegatesTextsCyrillic } from './Delegates.constants';
import { useColumns } from './hooks/useColumns';
import { useModal } from './context/modal.context';

import styles from './Delegates.module.scss';

const Delegates: FC = observer(() => {
  const { [StoreNames.delegatesStore]: delegatesStore } = useAppStoreContext();
  const { pageSetting, setPageSetting } = usePagination({ page: 0, size: 10 });
  const { openAdd } = useModal();
  const { isMobile } = usePlatformDetect();
  const columns = useColumns();
  const totalElements = delegatesStore.delegateResponseInfo.totalElements;

  const getDelegates = () => {
    delegatesStore.getDelegates(pageSetting);
  };

  useEffect(() => getDelegates(), [pageSetting]);

  if (!delegatesStore.isLoadingDelegates && !totalElements) {
    return (
      <>
        <EmptyDelegates />
        <DelegateAddEditModal refetch={getDelegates} />
      </>
    );
  }

  return (
    <div className={styles.wrapper}>
      <div className={styles.content}>
        <h1 className={styles.title}>{DelegatesTextsCyrillic[DelegatesTexts.listTitle]}</h1>
        {isMobile ? (
          delegatesStore.delegates.map(delegate => <DelegateCard key={delegate.id} delegate={delegate} />)
        ) : (
          <Table
            dataSource={delegatesStore.delegates}
            loading={delegatesStore.isLoadingDelegates}
            columns={columns}
            className={styles.table}
            rowClassName={(_, i) => (i % 2 === 1 ? styles.rowDark : '')}
            rowKey="id"
            tableLayout="auto"
            pagination={false}
          />
        )}
      </div>
      <Button
        type="primary"
        size="middle"
        className={styles.addButton}
        onClick={openAdd}
      >
        {DelegatesTextsCyrillic[DelegatesTexts.add]}
      </Button>
      {!!totalElements && (
        <Pagination
          showLessItems={isMobile}
          pagination={pageSetting}
          total={totalElements}
          setPagination={setPageSetting}
          className={styles.pagination}
        />
      )}
      <DelegateAddEditModal refetch={getDelegates} />
      <DelegateDeleteModal refetch={getDelegates} />
    </div>
  );
});

export default Delegates;
