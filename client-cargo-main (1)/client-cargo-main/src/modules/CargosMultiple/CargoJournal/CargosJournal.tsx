import React, { useEffect, useState } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { List, Pagination, Tabs } from 'antd';
import ruRU from 'antd/lib/locale/ru_RU';
import classNames from 'classnames';
import { observer } from 'mobx-react';
import { SpinWrapped } from 'shared/components';
import { CargoLayout, CargoLayoutWrapper } from 'shared/components/Cargo/CargoLayout';
import { TabsNavOption } from 'shared/components/TabsNav';
import { useTabsNavProps } from 'shared/components/TabsNav/useTabsNavProps';
// import { CargoTabFilters } from '../CargoTabFilters/CargoTabFilters';
import { useUiContext } from 'shared/components/UI';
import { EmptyApprovementList, EmptyRequestList } from 'shared/EmptyFactory';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useModalState } from 'shared/hooks/useModal';
import { useRouteParamSub } from 'shared/hooks/useRouteParamSub';
import { joinUrl } from 'utils';

import { ICargoRequestSearch } from 'stores/Cargos/Cargos.interface';
import { CargoRequestModel } from 'stores/Cargos/models/CargoRequest.model';
import { FilterSettingsType } from 'stores/Cargos/types';
import { StoreNames } from 'stores/StoreNames.enum';
import { CargosTabsFilters } from 'constants/Cargo.constants';
import { CARGOS, REGULAR_CARGOS } from 'constants/constants.routes';
import { SortProperty } from 'components/SortMenu/SortConstants';
import { SortMenu } from 'components/SortMenu/SortMenu';

import { CargoRequestListItem } from './CargoRequestListItem/CargoRequestListItem';
import { DateRangeFilter } from './DateRangeFilter/DateRangeFilter';
import { FilterButton } from './Filters/FilterButton';
import { Filters } from './Filters/Filters';
import { useCreationDateRange } from './hooks/useDesiredDateRange';
import { useResetSessionStorage } from './hooks/useResetSessionStorage';
import { useSaveFilters } from './hooks/useSaveFilters';
import { SelectApprovalControls } from './SelectApprovalControls/SelectApprovalControls';

import styles from './styles.module.scss';

const CargosJournal: React.FC<{
  tabs: TabsNavOption[];
  setTabs: (params: TabsNavOption[]) => void;
  tabsMain?: TabsNavOption[];
  activeLabelName: string;
  finalLabelName: string;
  fetchDataConfig: Record<string, (params: ICargoRequestSearch) => Promise<void>>;
  isApproval: boolean;
  isRegular?: boolean;
}> = observer(
  ({
    tabs,
    setTabs,
    tabsMain,
    activeLabelName,
    finalLabelName,
    fetchDataConfig,
    isApproval,
    isRegular,
  }) => {
    const {
      [StoreNames.cargosStore]: cargosStore,
      [StoreNames.selfStore]: selfStore,
    } = useAppStoreContext();

    const match = useRouteMatch();
    const history = History();
    const { setCheckedListApproval } = cargosStore;
    const { id } = selfStore.selfEmployee;

    const [isLoading, setIsLoading] = useState(true);
    const { isMobile } = useUiContext();
    const [isSortMenuVisible, setSortMenuVisible] = useState(false);
    const creationDate = useCreationDateRange();
    const [modal, modalActions] = useModalState();
    const { show, hide } = modalActions;
    const { getFilterState, filledCountStorage } = useSaveFilters();

    useResetSessionStorage();

    const regularCargo = isRegular ? 'Regular' : '';
    const requestType = isApproval ? 'Approval' : 'Request';

    const tabProps = useTabsNavProps();
    const { activeTab = CargosTabsFilters.active } = tabProps;
    const { totalElements } = isRegular ? cargosStore.paginationRegular : cargosStore.pagination;

    const nameList = isApproval ? 'cargoMultipleApprovalList' : `cargo${regularCargo}${requestType}List`;

    const { sortSetting, tabFilterName } = cargosStore.settings[nameList][activeTab];

    const { sortingOrders } = sortSetting;

    const CURRENT_SETTINGS = cargosStore.settings[nameList][activeTab];

    // Получение данных из стора в зависимости от типа доставки
    const cargosData
      = (cargosStore[
        isApproval ? 'cargoMultipleApprovalList' : `cargo${regularCargo}${requestType}List`
      ] as CargoRequestModel[]) || [];

    const requestIds = cargosData.map(el => el.id);

    const handleSortMenuToggle = () => {
      setSortMenuVisible(!isSortMenuVisible);
    };

    const itemClickHandler = (id: string): void => {
      history.push(joinUrl(`${match.url}/${id}`));
    };

    const handleRequest = (data?: FilterSettingsType) => {
      // Удаляем role из filtersData и используем его значение как ключ
      const {
        role, transportTypeEnum, ...restFilters
      } = data || {};
      const {
        desiredDate, shipmentTime, requestHumanId,
      } = restFilters;

      // Формируем объект запроса для фильтра
      const request = {
        ...restFilters,
        desiredDate: desiredDate ? { start: desiredDate[0], end: desiredDate[1] } : undefined,
        shipmentTime: shipmentTime ? { start: shipmentTime[0], end: shipmentTime[1] } : undefined,
        transportTypeEnum: transportTypeEnum?.length ? transportTypeEnum : undefined,
        creationDate,
        requestHumanId: requestHumanId ? requestHumanId : undefined,
        pageSetting: cargosStore.pageSetting,
        sortSetting: {
          directionAsc: sortSetting.directionAsc,
          property: sortSetting.property,
        },
      };
      // Если role === 'all', удаляем его из запроса
      if (role && role !== 'all') {
        request[role] = id;
      }

      return request;
    };

    const refetchData = async (filtersData?: FilterSettingsType) => {
      setIsLoading(true);
      const request = handleRequest(filtersData);

      try {
        await fetchDataConfig[activeTab](request);
      } catch (err) {
        console.error('Ошибка при перезагрузке данных:', err);
      } finally {
        setIsLoading(false);
      }
    };

    const approvalList
      = cargosData?.length > 0 ? (
        cargosData?.map(item => (
          <CargoRequestListItem
            key={item.id}
            request={item}
            onClickHandler={itemClickHandler}
            accentColor="#666666"
            isApproval={isApproval}
            isRegular={isRegular}
            activeTab={activeTab}
            tabFilterName={tabFilterName}
            onSuccess={refetchData}
          />
        ))
      ) : (
        !isApproval ? <EmptyRequestList /> : <EmptyApprovementList />
      );

    useEffect(() => {
      setIsLoading(true);
      cargosStore.setKeyFilterStateMain(isRegular);

      const filterSettings = getFilterState();
      const request = handleRequest(filterSettings);

      (async () => {
        try {
          await fetchDataConfig[activeTab](request);
        } catch (err) {
          // eslint-disable-next-line no-console
          console.error(err);
        } finally {
          setIsLoading(false);
        }
      })();
      cargosStore.setNameList(nameList);
    }, [
      sortSetting,
      tabFilterName,
      cargosStore.pageSetting.page,
      cargosStore.pageSetting.size,
      cargosStore.desiredDateRange,
    ]);

    useEffect(() => {
      // eslint-disable-next-line no-param-reassign
      tabs[0].label = activeLabelName;
      // eslint-disable-next-line no-param-reassign
      tabs[1].label = finalLabelName;

      setTabs(tabs);
    }, [totalElements, activeTab]);

    useEffect(() => {
      // Важно!
      // Сброс списка выбранных согласований
      if (isApproval) {
        setCheckedListApproval([]);
      }
    }, [activeTab, cargosStore.pageSetting.page]);

    const [statusFilter] = useRouteParamSub('filter', {
      options: Object.values(CargosTabsFilters),
      isStrict: true,
    });

    // Сортировка
    const handleSortChange = (directionAsc: boolean, property: SortProperty) => {
      cargosStore.setActiveSettings(nameList, activeTab, {
        ...CURRENT_SETTINGS,
        sortSetting: {
          directionAsc,
          property,
          sortingOrders: cargosStore.sortMapping[property][directionAsc.toString()],
        },
      });
    };

    // Переключить страницу
    const handlePageChange = (_page: number) => cargosStore.setPageSetting({
      ...cargosStore.pageSetting, page: _page - 1,
    });

    // Изменить размер страницы
    const handleSizeChange = (_current: number, _size: number) => cargosStore.setPageSetting({
      ...cargosStore.pageSetting,
      size: _size,
    });

    // Переключение табов верхнего уровня (Активные/Завершённые)
    const handleSwitchTab = key => {
      cargosStore.setPageSetting({ ...cargosStore.initialPageSetting });
      cargosStore.setActiveSettings(nameList, activeTab, {
        tabFilterName: cargosStore.initialTabFilterName,
      });
      if (key === 'final' || key === 'active') {
        history.push({ pathname: key });
      }
    };

    // Переключение табов нижнего уровня (Заявки/Расписания)
    const handleSwitchTabMain = (key: string) => {
      cargosStore.setPageSetting({ ...cargosStore.initialPageSetting });
      cargosStore.setActiveSettings(nameList, activeTab, {
        tabFilterName: cargosStore.initialTabFilterName,
      });
      if (key === 'regular/list/active') {
        history.push({ pathname: `${REGULAR_CARGOS}/active` });
      }
      if (key === 'single/list/active') {
        history.push({ pathname: `${CARGOS}/active` });
      }
    };

    // Переключение табов фильтра (Все, Инициатор, Отправитель, Получатель - теперь в фильтре)
    // Пока не удаляю, нужно потестить
    // const handleSwitchTabFilters = (tabName: FiltersTabs | FiltersTabsRegular) => {
    //   cargosStore.setActiveSettings(nameList, activeTab, {
    //     ...CURRENT_SETTINGS,
    //     tabFilterName: tabName,
    //     sortSetting: cargosStore.initialSortSetting,
    //   });
    //   cargosStore.setPageSetting({ ...cargosStore.initialPageSetting });
    // };

    return (
      <CargoLayoutWrapper>
        <CargoLayout>
          <div className={styles.headerDiv}>
            <div>
              {/* Верхний уровень табов: Активные/Завершённые */}
              <Tabs
                className={styles.MainTabs}
                activeKey={statusFilter}
                defaultActiveKey={tabs[0].label as string}
                onChange={handleSwitchTab}
                items={tabs.map(option => {
                  return {
                    label: option.label,
                    key: option.key,
                  };
                })}
              />
              {/* Нижний уровень табов: Заявки/Расписания (только для не-согласований) */}
              {!isApproval && (
                <div style={{ marginTop: '16px' }}>
                  <Tabs
                    className={styles.Tabs}
                    defaultActiveKey={cargosStore.keyFilterStateMain}
                    activeKey={cargosStore.keyFilterStateMain}
                    onChange={handleSwitchTabMain}
                    items={tabsMain?.map(option => {
                      return {
                        label: option.label,
                        key: option.key,
                      };
                    })}
                  />
                </div>
              )}

              {/* Пока не удаляю, нужно потестить */}
              {/* Фильтры (Все, Инициатор, Отправитель, Получатель) */}
              {/* {!isApproval && !isRegular && ( */}
              {/*  <div style={{ marginTop: '16px' }}> */}
              {/*    <CargoTabFilters */}
              {/*      tabFilterName={tabFilterName} */}
              {/*      onChange={value => { */}
              {/*        handleSwitchTabFilters(value as FiltersTabs | FiltersTabsRegular); */}
              {/*      }} */}
              {/*      isRegular={isRegular} */}
              {/*    /> */}
              {/*  </div> */}
              {/* )} */}
            </div>
            <div
              className={styles.menu}
              onClick={isMobile ? handleSortMenuToggle : undefined}
            >
              <SortMenu
                activeTab={activeTab}
                onSortChange={handleSortChange}
                sortSetting={sortSetting}
                name={sortingOrders}
              />
              {!isApproval && (
                <div style={{ display: 'flex', gap: '16px' }}>
                  <DateRangeFilter />
                  <FilterButton
                    filledCount={filledCountStorage()}
                    handleFilters={() => show()}
                  />
                </div>
              )}
            </div>
          </div>
          <div className={classNames(styles.listWrapper, {
            [styles.listWrapperJournal]: !isRegular && !isApproval,
            [styles.listWrapperRegular]: isRegular && !isApproval,
          })}
          >
            {isApproval && activeTab === CargosTabsFilters.active && (
              <SelectApprovalControls
                requestIds={requestIds}
                isRegular={isRegular}
                isApproval={isApproval}
                activeTab={activeTab}
              />
            )}
            {isLoading ? (
              <SpinWrapped />
            ) : (
              <List dataSource={cargosData}>{approvalList}</List>
            )}
          </div>
          <div className={styles.pagination}>
            <Pagination
              current={cargosStore.pageSetting.page + 1}
              defaultCurrent={cargosStore.pageSetting.page + 1}
              pageSize={cargosStore.pageSetting.size}
              total={totalElements}
              onChange={handlePageChange}
              onShowSizeChange={handleSizeChange}
              totalBoundaryShowSizeChanger={cargosStore.pageSetting.size}
              locale={ruRU.Pagination}
              showSizeChanger
            />
          </div>
        </CargoLayout>
        <Filters
          visible={modal}
          hideModal={hide}
          refetchData={refetchData}
        />
      </CargoLayoutWrapper>
    );
  }
);

export default CargosJournal;
