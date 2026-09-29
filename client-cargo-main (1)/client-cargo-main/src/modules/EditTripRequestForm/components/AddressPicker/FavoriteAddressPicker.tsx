import React, { MouseEvent, ReactNode } from 'react';
import { useBoolean } from 'ahooks';
import { Button, List } from 'antd';
import Modal from 'antd/lib/modal/Modal';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { AddressModel } from 'stores/Address/models/Address.model';
import { StoreNames } from 'stores/StoreNames.enum';

import styles from './styles.module.scss';

export const FavoriteAddressPicker = observer(({ onSelect }: { onSelect: (item: AddressModel) => void }) => {
  const { [StoreNames.addressStore]: address } = useAppStoreContext();
  const [modal, { setTrue: show, setFalse: hide }] = useBoolean();

  const clickItemHandler = (e: MouseEvent<any>, item: AddressModel): void => {
    e.stopPropagation();
    if (modal) {
      hide();
    }
    onSelect(item);
  };

  return (
    <>
      <Button
        onClick={show}
        shape="round"
        size="small"
        className={styles.button}
      >
        Любимые адреса
      </Button>
      <Modal
        title="Любимые адреса"
        open={modal}
        onCancel={hide}
        onOk={hide}
        cancelText="Назад"
        okButtonProps={{ style: { display: 'none' } }}
      >
        <List
          dataSource={address.selfFavoriteList}
          locale={{ emptyText: 'У вас нет любимых адресов' }}
          renderItem={(item: AddressModel): ReactNode => (
            <List.Item
              key={item.id}
              onClick={(e): void => clickItemHandler(e, item)}
              className={styles.item}
            >
              {item.label ? (
                <span className={styles.itemSpan}>
                  <b>{item.label}</b>
                  <div>
                    <small>
                      {' '}
                      (
                      {item.addressString}
                      )
                    </small>
                  </div>
                </span>
              ) : (
                <span>{item.addressString}</span>
              )}
            </List.Item>
          )}
        />
      </Modal>
    </>
  );
});
