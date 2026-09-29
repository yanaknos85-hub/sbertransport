import { List, Pagination } from 'antd';
import React, { FC, ReactNode } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';

import { useGetRequestSearchApproval } from 'api/approvals';

import { EmployeeAppLinksTitles } from 'constants/constants.app';
import { EmptyApprovementList } from 'shared/components/EmptyFactory/EmptyFactory';

import { SpinWrapped } from 'shared/components';
import PageLayout from 'shared/components/PageLayout/PageLayout';
import { TabsNav, TabsNavOption } from 'shared/components/TabsNav';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { usePagination } from 'shared/hooks/usePagination';
import { ApprovalTypeEnum } from 'shared/models/Approval.interface';
import { ApprovalModel } from 'shared/models/Approval.model';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { plainToNew } from 'utils';

import { ApprovalFilter } from './ApprovalFilter';
import ApprovalItem from './ApprovalItem';
import { useApprovalFilterProps } from './hooks/useApprovalFilterProps';

import styles from './list.module.scss';
import { getApprovalSettings } from './utils';

export interface CommonParams {
  transportType?: TransportTypeEnum;
  page: number;
  size: number;
}
const ApprovalList: FC = () => {
  const history = useHistory();
  const match = useRouteMatch<{ filter: 'active' | 'closed' }>();
  const { filter } = match.params;

  const view = (approval: ApprovalModel): void => {
    // В случае согласованной заявки (UPDATED_TRIP), id согласования получается из хука useGetApprovalByRequestId
    // и поэтому его не нужно брать из адресной строки
    const approvalId = approval.approvalType === ApprovalTypeEnum.UPDATED_TRIP ? '' : approval.id;
    const targetPath = `${match.url}/${String(approval.approvalType).toLowerCase()}/${
      approval.requestId
    }/${approvalId}`;
    history.push(targetPath);
  };

  const approvalFilterProps = useApprovalFilterProps(filter);

  const {
    setStatusSetting, transportType, statusSetting,
  } = approvalFilterProps;
  const {
    pageSetting, onPaginationChange, resetPagination,
  } = usePagination();
  const commomParams = {
    transportType,
    page: pageSetting.page,
    size: pageSetting.size,
  };
  const settings = getApprovalSettings(filter, statusSetting, commomParams);
  const {
    data: data1, isLoading, refetch,
  } = useGetRequestSearchApproval(settings);
  const approvalLists = plainToNew<ApprovalModel[]>(ApprovalModel, data1?.content);
  const tabs: TabsNavOption[] = [
    { key: 'active', label: 'Активные' },
    { key: 'closed', label: 'Завершённые' },
  ];

  const clickHandlerTabs = () => {
    setStatusSetting('');
    resetPagination();
  };
  return (
    <PageLayout title={EmployeeAppLinksTitles['requests']} contentClassName={styles.content}>
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
            <ApprovalFilter {...approvalFilterProps} />
            <List
              header={`Всего согласований: ${data1?.totalElements}`}
              className={styles.list}
              dataSource={approvalLists}
              locale={{ emptyText: <EmptyApprovementList /> }}
              renderItem={(approval: ApprovalModel): ReactNode => (
                // Надо проверить как отрабатывают согласования потому что набэке не происходит обновление get запроса
                <ApprovalItem
                  approval={approval}
                  view={view}
                  key={approval.id}
                  refetchApprovals={refetch}
                />
              )}
            />
            <div className={styles.pagination}>
              <Pagination
                current={pageSetting.page + 1}
                defaultCurrent={pageSetting.page + 1}
                total={data1?.totalElements}
                pageSize={pageSetting.size}
                onChange={onPaginationChange}
                hideOnSinglePage={true}
                showSizeChanger={false}
              />
            </div>
          </>
        )}
      </div>
    </PageLayout>
  );
};

export default withErrorBoundary(ApprovalList);
