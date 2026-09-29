import { Pagination } from 'antd';
import React, { useEffect, type FC } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';

import { plainToNew } from 'utils';

import { useGetRequestSearchApproval } from 'api/approvals';

import { EmployeeAppLinks, EmployeeAppLinksTitles } from 'modules/EmployeeApp/EmployeeApp.constants';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { SpinWrapped } from 'shared/components';
import PageLayout from 'shared/components/PageLayout/PageLayout';
import { TabsNav, TabsNavOption } from 'shared/components/TabsNav';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { ApprovalTypeEnum } from 'shared/models/Approval.interface';
import { ApprovalModel } from 'shared/models/Approval.model';

import { ApprovalFilter } from './ApprovalFilter';
import { getApprovalSettings } from './utils';
import { ApprovalListItem } from './ApprovalListItem/ApprovalListItem';
import { EmptyApprovementList } from 'modules/EmployeeApp/shared/EmptyFactory';

import styles from './list.module.scss';

export interface CommonParams {
  transportType?: TransportTypeEnum;
  page: number;
  size: number;
}
const ApprovalList: FC = observer(() => {
  const {
    [StoreNames.configStore]: configStore,
    [StoreNames.approvalsStore]: approvalsStore,
  } = useAppStoreContext();
  const match = useRouteMatch<{ filter: 'active' | 'closed' }>();
  const { filter } = match.params;

  const view = (approval: ApprovalModel): void => {
    // В случае согласованной заявки (UPDATED_TRIP), id согласования получается из хука useGetApprovalByRequestId
    // и поэтому его не нужно брать из адресной строки
    const approvalId = approval.approvalType === ApprovalTypeEnum.UPDATED_TRIP ? '' : approval.id;
    const targetPath = `${match.url}/${String(approval.approvalType).toLowerCase()}/${
      approval.requestId
    }/${approvalId}`;
    configStore.history.push(targetPath);
  };

  useEffect(() => {
    approvalsStore.setFilter(filter);
  }, [filter]);

  useEffect(() => {
    if (!approvalsStore.isFromDetailedPage) {
      approvalsStore.resetPagination();
      approvalsStore.resetFilters();
    }

    return () => {
      approvalsStore.setDetailedPageStatus(false);
    };
  }, []);

  const commonParams = {
    transportType: approvalsStore.filtersSettings.transportType,
    page: approvalsStore.paginationSettings.page,
    size: approvalsStore.paginationSettings.size,
  };
  const settings = getApprovalSettings(filter, approvalsStore.filtersSettings.statusSettings, commonParams);
  const {
    data: data1, isLoading, refetch,
  } = useGetRequestSearchApproval(settings);
  const approvalLists = plainToNew<ApprovalModel[]>(ApprovalModel, data1?.content);
  const tabs: TabsNavOption[] = [
    { key: 'active', label: 'Активные' },
    { key: 'closed', label: 'Завершённые' },
  ];

  const clickHandlerTabs = () => {
    approvalsStore.resetFilters();
    approvalsStore.resetPagination();
  };
  return (
    <PageLayout title={EmployeeAppLinksTitles[EmployeeAppLinks.approvalRequestList]} contentClassName={styles.content}>
      <div className={styles.content}>
        <TabsNav
          currentKey={filter}
          options={tabs}
          defaultActiveTab={tabs[0]}
          onTabClick={clickHandlerTabs}
        />
        <ApprovalFilter disabled={isLoading} />
        {isLoading ? (
          <SpinWrapped />
        ) : (
          <>
            {approvalLists?.length ? (
              <div className={styles.approvalsList}>
                {approvalLists.map(approval => (
                  <ApprovalListItem
                    approval={approval}
                    view={view}
                    refetchApprovals={refetch}
                    key={approval.id}
                  />
                ))}
              </div>
            ) : <EmptyApprovementList />}
            <div className={styles.pagination}>
              <Pagination
                current={approvalsStore.paginationSettings.page + 1}
                defaultCurrent={approvalsStore.paginationSettings.page + 1}
                total={data1?.totalElements}
                pageSize={approvalsStore.paginationSettings.size}
                onChange={approvalsStore.setPagination}
                hideOnSinglePage
                showSizeChanger={false}
              />
            </div>
          </>
        )}
      </div>
    </PageLayout>
  );
});

export default withErrorBoundary(ApprovalList);

