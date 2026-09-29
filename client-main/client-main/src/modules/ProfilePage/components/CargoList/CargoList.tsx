import React, { FC, useEffect, useState } from 'react';
import { observer } from 'mobx-react';
import { Table } from 'antd';

import { StoreNames } from 'ioc/ioc.storeNames';

import { ReactComponent as ArrowUp } from 'shared/components/Images/arrowUp.svg';
import { ReactComponent as ArrowDown } from 'shared/components/Images/arrowDown.svg';

import { useColumns } from './hooks/useColumns';

import { Pagination } from 'shared/components/Pagination/Pagination';
import { usePagination } from 'shared/hooks/usePagination';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import DeleteCargoModal from './DeleteCargoModal/DeleteCargoModal';

import {
  OpenMainContent,
  StyledMyAddresses,
  StyledTitle,
  WrapperButtonOpenMainContent,
  WrapperOpenMainContent,
  WrapperStyledTitle
} from './styled';

import styles from './CargoList.module.scss';

const CargoList: FC = observer(() => {
  const [openWindow, setOpenWindow] = useState(false);
  const { [StoreNames.cargoListStore]: cargoListStore } = useAppStoreContext();
  const columns = useColumns();
  const { isMobile } = usePlatformDetect();
  const { pageSetting, setPageSetting } = usePagination({ page: 0, size: 10 });

  const getCargoListPersonal = () => {
    cargoListStore.getCargoListPersonal(pageSetting);
  };

  useEffect(() => getCargoListPersonal(), [pageSetting]);

  return (
    <>
      <StyledMyAddresses>
        <WrapperStyledTitle>
          <StyledTitle>Список грузов</StyledTitle>
          <WrapperOpenMainContent>
            {openWindow
              ? (
                <WrapperButtonOpenMainContent onClick={() => setOpenWindow(prev => !prev)}>
                  <OpenMainContent>
                    Cкрыть
                  </OpenMainContent>
                  <ArrowUp />
                </WrapperButtonOpenMainContent>
              )
              : (
                <WrapperButtonOpenMainContent onClick={() => setOpenWindow(prev => !prev)}>
                  <OpenMainContent>
                    Раскрыть
                  </OpenMainContent>
                  <ArrowDown />
                </WrapperButtonOpenMainContent>
              )}
          </WrapperOpenMainContent>
        </WrapperStyledTitle>
      </StyledMyAddresses>
      {openWindow && (
        <div className={styles.wrapper}>
          <div className={styles.content}>
            <Table
              dataSource={cargoListStore.cargoList?.content || []}
              columns={columns}
              pagination={false}
              className={styles.table}
            />
          </div>
          <Pagination
            showLessItems={isMobile}
            pagination={pageSetting}
            total={cargoListStore.cargoList.totalElements}
            setPagination={setPageSetting}
          />
          <DeleteCargoModal refetch={getCargoListPersonal} />
        </div>
      )}
    </>
  );
});

export default CargoList;
