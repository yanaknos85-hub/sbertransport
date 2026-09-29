import React, { useEffect, useState } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { List, Pagination, Tabs } from 'antd';
import ruRU from 'antd/lib/locale/ru_RU';
import { observer } from 'mobx-react';
import { SpinWrapped } from 'shared/components';
import { useTabsNavProps } from 'shared/components/TabsNav/useTabsNavProps';
import { EmptyApprovementList } from 'shared/EmptyFactory';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { joinUrl } from 'utils';

import { ICompensationRequestSearch } from 'stores/Compensations/types';
import { StoreNames } from 'stores/StoreNames.enum';
import { activeLabelName, CargosTabsFilters, finalLabelName } from 'constants/Cargo.constants';
import { SortProperty } from 'components/SortMenu/SortConstants';
import { SortMenu } from 'components/SortMenu/SortMenu';

import { CompensationRequestListItem } from '../CompensationRequestListItem/CompensationRequestListItem';
import { SelectApprovalControls } from '../SelectApprovalControls/SelectApprovalControls';
import * as S from './styled';

const CompensationApprovalContent: React.FC<{
  fetchDataConfig: Record<string, (params: ICompensationRequestSearch) => Promise<void>>;
}> = observer(({ fetchDataConfig }) => {
  const {
    [StoreNames.compensationStore]: compensationStore,
    [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();

  const match = useRouteMatch();
  const history = History();
  const { id } = selfStore.selfEmployee;

  const [isLoading, setIsLoading] = useState(true);

  const NAME_LIST = 'compensationList';

  const tabs = [
    { key: CargosTabsFilters.active, label: activeLabelName },
    { key: CargosTabsFilters.final, label: finalLabelName },
  ];

  const { totalElements } = compensationStore.paginationCompensation;

  const tabProps = useTabsNavProps();
  const { activeTab = CargosTabsFilters.active } = tabProps;

  const { sortSetting } = compensationStore.settings[NAME_LIST][activeTab];
  const { sortingOrders } = sortSetting;
  const CURRENT_SETTINGS = compensationStore.settings[NAME_LIST][activeTab];

  const data = compensationStore.compensationList.content || [];
  const requestIds = data.map(el => el.id);

  const itemClickHandler = (id: string): void => {
    history.push(joinUrl(`${match.url}/${id}`));
  };

  const approvalList = data.length > 0 ? (
    data.map(item => (
      <CompensationRequestListItem
        key={item.id}
        request={item}
        onClickHandler={itemClickHandler}
        accentColor="#666666"
        isApproval={true}
        isRegular={false}
        activeTab={activeTab}
      />
    ))
  ) : (
    <EmptyApprovementList />
  );

  const handleSortChange = (directionAsc: boolean, property: SortProperty) => {
    compensationStore.setActiveSettings(NAME_LIST, activeTab, {
      ...CURRENT_SETTINGS,
      sortSetting: {
        directionAsc,
        property,
        sortingOrders: compensationStore.sortMapping[property][directionAsc.toString()],
      },
    });
  };

  const handlePageChange = (page: number) => compensationStore.setPageSetting({ ...compensationStore.pageSetting, page: page - 1 });

  const handleSizeChange = (_current: number, size: number) => compensationStore.setPageSetting({ ...compensationStore.pageSetting, size });

  const handleSwitchTab = (key: string) => {
    compensationStore.resetApprovalJournalFilters(NAME_LIST, activeTab);
    history.push(key);
  };

  useEffect(() => {
    let isMounted = true;

    const fetchData = async () => {
      if (!isMounted) return;

      setIsLoading(true);
      compensationStore.compensationList.content = [];
      const request: ICompensationRequestSearch = {
        pageSetting: compensationStore.pageSetting,
        sortSetting: {
          directionAsc: sortSetting.directionAsc,
          property: sortSetting.property,
        },
      };

      try {
        await fetchDataConfig[activeTab](request);
      } catch (err) {
        console.error(err);
      } finally {
        setIsLoading(false);
      }

      compensationStore.setNameList(NAME_LIST);
    };

    fetchData();

    return () => {
      isMounted = false;
    };
  }, [
    activeTab,
    sortSetting.property,
    sortSetting.directionAsc,
    compensationStore.pageSetting.page,
    compensationStore.pageSetting.size,
    id,
  ]);

  useEffect(() => {
    compensationStore.setCheckedListApproval([]);
  }, [activeTab, compensationStore.pageSetting.page]);

  if (isLoading) {
    return <SpinWrapped />;
  }

  return (
    <>
      <S.HeaderContainer>
        <Tabs activeKey={activeTab} onChange={handleSwitchTab}>
          {tabs.map(option => (
            <Tabs.TabPane tab={option.label} key={option.key} />
          ))}
        </Tabs>
        <S.MenuContainer>
          {data.length > 1 && (
            <SortMenu
              activeTab={activeTab}
              onSortChange={handleSortChange}
              sortSetting={sortSetting}
              name={sortingOrders}
            />
          )}
        </S.MenuContainer>
      </S.HeaderContainer>
      <S.ListWrapper>
        {activeTab === CargosTabsFilters.active && data.length > 1 && (
          <SelectApprovalControls
            requestIds={requestIds}
            isRegular={false}
            isApproval={true}
            activeTab={activeTab}
          />
        )}
        <List dataSource={data}>{approvalList}</List>
      </S.ListWrapper>
      <S.PaginationContainer>
        <Pagination
          current={compensationStore.pageSetting.page + 1}
          pageSize={compensationStore.pageSetting.size}
          total={totalElements}
          onChange={handlePageChange}
          onShowSizeChange={handleSizeChange}
          showSizeChanger
          locale={ruRU.Pagination}
        />
      </S.PaginationContainer>
    </>
  );
});

export default CompensationApprovalContent;
