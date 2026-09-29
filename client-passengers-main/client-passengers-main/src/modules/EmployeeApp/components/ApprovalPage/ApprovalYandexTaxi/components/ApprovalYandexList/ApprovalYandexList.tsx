import React, { useEffect, type FC } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { Pagination } from 'antd';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';

import { EmployeeAppLinks, EmployeeAppLinksTitles } from 'modules/EmployeeApp/EmployeeApp.constants';

import { SpinWrapped } from 'shared/components';
import PageLayout from 'shared/components/PageLayout/PageLayout';
import { TabsNav, TabsNavOption } from 'shared/components/TabsNav';
import { usePagination } from 'shared/hooks/usePagination';
import { ApprovalListItem } from '../ApprovalListItem/ApprovalListItem';

import { EmptyApprovementList } from 'modules/EmployeeApp/shared/EmptyFactory';

import { activeStatuses, closedStatuses } from '../../constants/yandexTaxi.constants';
import { useYandexTaxiApprovals } from 'api/yandexTaxi/yandex-taxi.api';

import styles from './approvalYandexList.module.scss';

const ApprovalYandexList: FC = () => {
  const match = useRouteMatch<{ filter: 'active' | 'closed' }>();
  const { filter } = match.params;

  const {
    pageSetting, onPaginationChange, resetPagination,
  } = usePagination();

  const {
    data: { content: approvalList, page }, isLoading, refetch,
  } = useYandexTaxiApprovals({
    requestStatusSet: filter === 'active' ? activeStatuses : closedStatuses,
    pageSetting,
  });

  const tabs: TabsNavOption[] = [
    { key: 'active', label: 'Активные' },
    { key: 'closed', label: 'Завершённые' },
  ];

  useEffect(
    () => {
      if (pageSetting.page !== page.number || pageSetting.size !== page.size) {
        refetch();
      }
    },
    [pageSetting.page, pageSetting.size, page]
  );

  useEffect(
    () => {
      resetPagination();
      refetch();
    },
    [filter]
  );

  const clickHandlerTabs = () => {
    resetPagination();
  };

  return (
    <PageLayout title={EmployeeAppLinksTitles[EmployeeAppLinks.yandex]} contentClassName={styles.content}>
      <div className={styles.content}>
        {isLoading ? (
          <SpinWrapped />
        ) : (
          <>
            <TabsNav
              currentKey={filter}
              options={tabs}
              defaultActiveTab={tabs[0]}
              onTabClick={clickHandlerTabs}
            />
            {approvalList?.length ? (
              <div className={styles.approvalsList}>
                {approvalList.map(approval => (
                  <ApprovalListItem
                    approval={approval}
                    refetchApprovals={refetch}
                    key={approval.id}
                  />
                ))}
              </div>
            ) : <EmptyApprovementList />}
            <div className={styles.pagination}>
              <Pagination
                current={pageSetting.page + 1}
                defaultCurrent={pageSetting.page + 1}
                total={page.total || 0}
                pageSize={pageSetting.size}
                onChange={onPaginationChange}
                showSizeChanger={true}
              />
            </div>
          </>
        )}
      </div>
    </PageLayout>
  );
};

export default withErrorBoundary(ApprovalYandexList);
