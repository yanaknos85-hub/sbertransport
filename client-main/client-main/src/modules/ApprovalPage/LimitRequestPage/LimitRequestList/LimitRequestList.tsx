import React, { ReactNode } from 'react';
import { observer } from 'mobx-react';
import { List } from 'antd';

import { TabsNav } from 'shared/components/TabsNav';
import { EmptyDataList } from 'shared/components/EmptyFactory/EmptyFactory';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';
import { Pagination } from 'shared/components/Pagination/Pagination';

import { LimitRequestInfo } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { LimitRequestFilter } from '../../components/LimitRequestFilter/LimitRequestFilter';
import { LimitRequestListItem } from './LimitRequestListItem';
import { useLimitRequestList } from './useLimitRequestList';
import styles from './list.module.scss';

const LimitRequestList = observer(() => {
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();
  const { isMobile } = usePlatformDetect();

  const {
    requestList,
    tabOptions,
    tabKey,
    filterProps,
    isActiveFilter,
    pageSetting,
    refetch,
    setPageSetting,
  } = useLimitRequestList();

  const handleTabChange = () => {
    filterProps.setTransportType(undefined);
    setPageSetting({ page: 0, size: 10 });
  };

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.content}>
        <TabsNav
          currentKey={tabKey}
          options={tabOptions}
          defaultActiveTab={tabOptions[0]}
          onTabClick={handleTabChange}
        />
        <LimitRequestFilter
          totalElements={requestList?.totalElements}
          isActiveFilter={isActiveFilter}
          setPageSetting={setPageSetting}
          {...filterProps}
        />
        <List
          loading={limitsStore.limitsRequestsIsLoading}
          className={styles.list}
          dataSource={requestList?.content || []}
          locale={{ emptyText: <EmptyDataList title="У вас нет заявок для согласования" /> }}
          renderItem={(request: LimitRequestInfo): ReactNode => (
            <LimitRequestListItem
              request={request}
              viewDisabled={!isActiveFilter}
              refresh={refetch}
            />
          )}
        />
        {!!requestList?.totalElements && (
          <Pagination
            pagination={pageSetting}
            total={requestList?.totalElements}
            showLessItems={isMobile}
            setPagination={setPageSetting}
            className={styles.pagination}
          />
        )}
      </div>
    </div>
  );
});

export default LimitRequestList;
