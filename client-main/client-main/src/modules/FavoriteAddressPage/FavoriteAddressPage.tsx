import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, List } from 'antd';
import { observer } from 'mobx-react';
import React, {
  FC, ReactNode, useEffect, useState
} from 'react';

import { EmployeeAppLinksTitles } from 'constants/constants.app';
import { EmptyFavoriteList } from 'shared/components/EmptyFactory/EmptyFactory';

import { ReactComponent as BookmarkEnabledIcon } from 'shared/components/Images/view/bookmark_enabled.svg';
import PageLayout from 'shared/components/PageLayout/PageLayout';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { AddressModel } from 'stores/Address/models/Address.model';

import AddFavoriteModal from './AddFavoriteModal';

import styles from './favorite.module.scss';

const FavoriteAddressPage: FC = observer(() => {
  const { addressStore } = useAppStoreContext();

  useEffect(() => {
    addressStore.initStore();
  }, [addressStore]);

  const [modal, setModal] = useState(false);
  const showModal = (): void => setModal(true);
  const hideModal = (): void => setModal(false);

  const addBtn = (
    <Button
      type="dashed"
      onClick={showModal}
      icon={<PlusOutlined />}
    />
  );

  const deleteFavorite = (id: string): void => {
    addressStore.deleteFavoriteAddress(id);
  };

  const deleteFrequent = (id: string): void => {
    addressStore.deleteFrequentAddress(id);
  };

  return (
    <>
      <PageLayout
        title={EmployeeAppLinksTitles.favorite}
        extra={addBtn}
        loading={addressStore.selfFavoriteList === undefined}
      >
        <List
          header={<div className="list-header">Любимые адреса</div>}
          className={styles.list}
          dataSource={addressStore.selfFavoriteList}
          locale={{ emptyText: <EmptyFavoriteList /> }}
          renderItem={(address: AddressModel): ReactNode => (
            <List.Item>
              {address.label ? (
                <span className={styles.listItemSpan}>
                  <b>{address.label}</b>
                  <div>
                    <small>
                      {' '}
                      (
                      {address.addressString}
                      )
                    </small>
                  </div>
                </span>
              ) : (
                <span>{address.addressString}</span>
              )}
              <Button
                type="ghost"
                icon={<DeleteOutlined />}
                onClick={(): void => deleteFavorite(address.id)}
              />
            </List.Item>
          )}
        />

        <List
          header={<div className={styles.listHeader}>Частые адреса</div>}
          className={styles.list}
          dataSource={addressStore.selfFrequentList}
          locale={{ emptyText: 'Тут пока ничего нет' }}
          renderItem={(address: AddressModel): ReactNode => (
            <List.Item style={{ alignItems: 'center' }}>
              <BookmarkEnabledIcon />
              <span>
                {address.addressString}
                {' '}
              </span>
              <Button
                type="ghost"
                icon={<DeleteOutlined />}
                onClick={(): void => deleteFrequent(address.id)}
              />
            </List.Item>
          )}
        />
      </PageLayout>
      <AddFavoriteModal
        visible={modal}
        onOk={hideModal}
        onCancel={hideModal}
      />
    </>
  );
});

export default FavoriteAddressPage;
