import React, { FC, useEffect } from 'react';
import { List as ListAntd, Pagination } from 'antd';
import ruRU from 'antd/lib/locale/ru_RU';
import { StoreNames } from 'ioc/ioc.storeNames';
import { observer } from 'mobx-react';
import { EmptyRequestFilterListExchange } from 'shared/EmptyFactory';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useModalState } from 'shared/hooks/useModal';

import { useGetFilters } from '../../api/filters';
import { ExchangeResponse } from '../../types';
import { ListItem } from '../ListItem/listItem';
import { FiltersModal } from '../Modals/FiltersModal/FiltersModal';
import { Controls } from './Controls/Controls';

import styles from './List.module.scss';

export const List: FC = observer(() => {
  const {
    [StoreNames.exchangeStore]: exchangeStore,
  } = useAppStoreContext();

  const { exchangeRequest } = exchangeStore;

  const { totalElements, content } = exchangeRequest as ExchangeResponse;

  const [modal, modalActions] = useModalState();

  // description AddressFrom/To — адреса, которые отображаются в Controls
  // savedFilters — сохранённые фильтры для загрузки в форму
  const [descriptionAddressFrom, setDescriptionAddressFrom] = React.useState<string>('');
  const [descriptionAddressTo, setDescriptionAddressTo] = React.useState<string>('');
  const { data: savedFiltersRaw } = useGetFilters();
  const savedFilters = savedFiltersRaw ?? undefined;

  // Обновляем адреса из savedFilters при изменении
  useEffect(() => {
    if (savedFilters) {
      setDescriptionAddressFrom(savedFilters.addressFrom || '');
      setDescriptionAddressTo(savedFilters.addressTo || '');
    }
  }, [savedFilters]);

  const handlePageChange = (_page: number) => exchangeStore.setPageSetting({ ...exchangeStore.pageSetting, page: _page - 1 });
  const handleSizeChange = (_current: number, _size: number) => exchangeStore.setPageSetting({
    ...exchangeStore.pageSetting,
    size: _size,
  });

  const listData = content?.length > 0 ? (content.map(request => {
    return <ListItem key={request.id} request={request} />;
  })) : <EmptyRequestFilterListExchange />;

  return (
    <>
      <Controls
        onShow={modalActions.show}
        descriptionAddressFrom={descriptionAddressFrom}
        descriptionAddressTo={descriptionAddressTo}
      />
      <div className={styles.listWrapper}>
        <ListAntd dataSource={content}>{listData}</ListAntd>
      </div>
      <div className={styles.pagination}>
        <Pagination
          current={exchangeStore.pageSetting.page + 1}
          defaultCurrent={exchangeStore.pageSetting.page + 1}
          total={totalElements}
          pageSize={exchangeStore.pageSetting.size}
          onChange={handlePageChange}
          onShowSizeChange={handleSizeChange}
          totalBoundaryShowSizeChanger={exchangeStore.pageSetting.size}
          locale={ruRU.Pagination}
          showSizeChanger={true}
        />
      </div>
      <FiltersModal
        visible={modal}
        handleClose={() => modalActions.hide()}
        setDescriptionAddressFrom={setDescriptionAddressFrom}
        setDescriptionAddressTo={setDescriptionAddressTo}
      />
    </>
  );
});
