import React, { FC, Suspense, useRef, useState, useCallback } from 'react';
import { observer } from 'mobx-react';
import { Table } from 'antd';
import { useTableConfig } from 'shared/hooks/useTableConfig';
import { useTableFields } from './components/Table/useTableFields';
import { useEtrnQuery } from './hooks/useEtrnQuery';
import { tableScrollConfiguration } from 'modules/Planner/Components/Monitor/utils';
import { Pagination } from 'modules/Planner/Pagination/Pagination';
import { EtrnFilters } from './components/Filters/EtrnFilters';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { EmptyView } from 'shared/components/EmptyView';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';
import EtrnModal from './components/Card/EtrnModal';

import styles from './styles.module.scss';

const EtrnSignature: FC = observer(() => {
  const tableRef = useRef<HTMLDivElement>(null);
  const tableConfig = useTableConfig(tableRef, tableScrollConfiguration);
  const [selectedCardId, setSelectedCardId] = useState<string>('');

  const { plannerStore } = useAppStoreContext();

  const { data, refetch } = useEtrnQuery();

  const isModalOpen = selectedCardId !== '';

  const handleOpenCard = useCallback((cardId: string) => {
    setSelectedCardId(cardId);
  }, []);

  const fields = useTableFields({ onOpenCard: handleOpenCard });

  const handleCloseCard = useCallback(() => {
    setSelectedCardId('');
  }, []);

  const handleSigned = useCallback(() => {
    refetch();
  }, [refetch]);


  return (
    <>
      <EtrnFilters />
      <Suspense fallback={<SpinWrapped />}>
        {data?.content?.length ? (
          <div ref={tableRef}>
            <Table
              rowKey="id"
              columns={fields}
              dataSource={data.content}
              className={styles.etrnTable}
              rowClassName={styles.etrnTable__row}
              scroll={tableConfig}
              pagination={false}
            />
            <div className={styles.etrnTable__pagination}>
              <Pagination
                pagination={{
                  page: plannerStore.setEtrnListPageSetting.page,
                  size: plannerStore.setEtrnListPageSetting.size,
                }}
                total={data.totalElements}
                setPagination={plannerStore.setEtrnPageSettings}
              />
            </div>
          </div>
        ) : (
          <EmptyView title="Нет данных" />
        )}
      </Suspense>
      <EtrnModal
        cardId={selectedCardId}
        visible={isModalOpen}
        onClose={handleCloseCard}
        onSigned={handleSigned}
      />
    </>
  );
});

export default EtrnSignature;
