import React, { useEffect, useState } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { List, Pagination, Tabs } from 'antd';
import ruRU from 'antd/lib/locale/ru_RU';
import classNames from 'classnames';
import { observer } from 'mobx-react';
import { SpinWrapped } from 'shared/components';
import { CargoLayout, CargoLayoutWrapper } from 'shared/components/Cargo/CargoLayout';
import { useTabsNavProps } from 'shared/components/TabsNav/useTabsNavProps';
import { EmptyApprovementList } from 'shared/EmptyFactory';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { joinUrl } from 'utils';

import { ICargoRequestSearch } from 'stores/Cargos/Cargos.interface';
import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';
import { StoreNames } from 'stores/StoreNames.enum';
import { activeLabelName, CargosTabsFilters, finalLabelName } from 'constants/Cargo.constants';
import { SortProperty } from 'components/SortMenu/SortConstants';
import { SortMenu } from 'components/SortMenu/SortMenu';

import { CargoRequestListItem } from '../CargoJournal/CargoRequestListItem/CargoRequestListItem';
import { SelectApprovalControls } from '../CargoJournal/SelectApprovalControls/SelectApprovalControls';

import styles from './styles.module.scss';

const CargoApprovalContent: React.FC<{
  isRegular: boolean;
  fetchDataConfig: Record<string, (params: ICargoRequestSearch) => Promise<void>>;
}> = observer(({ isRegular, fetchDataConfig }) => {
  const {
    [StoreNames.cargosStore]: cargosStore,
    [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();

  const match = useRouteMatch();
  const history = History();
  const { setCheckedListApproval } = cargosStore;
  const { id } = selfStore.selfEmployee;

  const [isLoading, setIsLoading] = useState(true);

  const NAME_LIST = 'cargoMultipleApprovalList';

  const tabs = [
    { key: CargosTabsFilters.active, label: activeLabelName },
    { key: CargosTabsFilters.final, label: finalLabelName },
  ];

  const { totalElements } = isRegular ? cargosStore.paginationRegular : cargosStore.pagination;
  const tabProps = useTabsNavProps();
  const { activeTab = CargosTabsFilters.active } = tabProps;

  const { sortSetting } = cargosStore.settings[NAME_LIST][activeTab];
  const { sortingOrders } = sortSetting;
  const CURRENT_SETTINGS = cargosStore.settings[NAME_LIST][activeTab];

  const cargosData = (cargosStore['cargoMultipleApprovalList'] as CargoRequestModel[]) || [];
  const requestIds = cargosData.map(el => el.id);

  // Основной эффект для загрузки данных - объединяем все зависимости
  useEffect(() => {
    let isMounted = true;

    const fetchData = async () => {
      if (!isMounted) return;

      setIsLoading(true);
      cargosStore.setKeyFilterStateMainApproval();

      const request: ICargoRequestSearch = {
        pageSetting: cargosStore.pageSetting,
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
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    fetchData();

    return () => {
      isMounted = false;
    };
  }, [
    activeTab,
    sortSetting.property,
    sortSetting.directionAsc,
    cargosStore.pageSetting.page,
    cargosStore.pageSetting.size,
    id,
  ]);

  // Эффект для сброса выбранных элементов при смене таба или страницы
  useEffect(() => {
    setCheckedListApproval([]);
  }, [activeTab, cargosStore.pageSetting.page]);

  const itemClickHandler = (id: string): void => {
    history.push(joinUrl(`${match.url}/${id}`));
  };

  const approvalList = cargosData.length > 0 ? (
    cargosData.map(item => (
      <CargoRequestListItem
        key={item.id}
        request={item}
        onClickHandler={itemClickHandler}
        accentColor="#666666"
        isApproval={true}
        isRegular={isRegular}
        activeTab={activeTab}
      />
    ))
  ) : (
    <EmptyApprovementList />
  );

  const handleSortChange = (directionAsc: boolean, property: SortProperty) => {
    cargosStore.setActiveSettings(NAME_LIST, activeTab, {
      ...CURRENT_SETTINGS,
      sortSetting: {
        directionAsc,
        property,
        sortingOrders: cargosStore.sortMapping[property][directionAsc.toString()],
      },
    });
  };

  const handlePageChange = (page: number) => cargosStore.setPageSetting({ ...cargosStore.pageSetting, page: page - 1 });

  const handleSizeChange = (_current: number, size: number) => cargosStore.setPageSetting({ ...cargosStore.pageSetting, size });

  const handleSwitchTab = (key: string) => {
    cargosStore.resetApprovalJournalFilters(NAME_LIST, activeTab);
    history.push(key);
  };

  if (isLoading) {
    return <SpinWrapped />;
  }

  return (
    <CargoLayoutWrapper>
      <CargoLayout>
        <div className={styles.headerDiv}>
          <Tabs
            activeKey={activeTab}
            onChange={handleSwitchTab}
          >
            {tabs.map(option => (
              <Tabs.TabPane tab={option.label} key={option.key} />
            ))}
          </Tabs>
          <div className={styles.menu}>
            {cargosData.length > 1 && (
              <SortMenu
                activeTab={activeTab}
                onSortChange={handleSortChange}
                sortSetting={sortSetting}
                name={sortingOrders}
              />
            )}
          </div>
        </div>
        <div className={classNames(styles.listWrapper, { [styles.listWrapperRegular]: isRegular })}>
          {activeTab === CargosTabsFilters.active && cargosData.length > 1 && (
            <SelectApprovalControls
              requestIds={requestIds}
              isRegular={isRegular}
              isApproval={true}
              activeTab={activeTab}
            />
          )}
          <List dataSource={cargosData}>{approvalList}</List>
        </div>
        <div className={styles.pagination}>
          <Pagination
            current={cargosStore.pageSetting.page + 1}
            pageSize={cargosStore.pageSetting.size}
            total={totalElements}
            onChange={handlePageChange}
            onShowSizeChange={handleSizeChange}
            showSizeChanger
            locale={ruRU.Pagination}
          />
        </div>
      </CargoLayout>
    </CargoLayoutWrapper>
  );
});

export default CargoApprovalContent;
