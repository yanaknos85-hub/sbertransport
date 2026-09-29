import React, { useState, FC } from 'react';
import Modal from 'antd/lib/modal';
import { observer } from 'mobx-react';

import { StoreNames } from 'stores/StoreNames.enum';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { ReactComponent as CloseIcon } from 'shared/images/cargo/closeIcon.svg';

interface DelegateDeleteProps {
  refetch: () => void;
}

const DeleteCargoModal: FC<DelegateDeleteProps> = observer(({ refetch }) => {
  const {
    [StoreNames.cargoListStore]: cargoListStore,
    [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();

  const { isModalVisible, setModalVisible } = cargoListStore;

  const [isLoading, setLoading] = useState(false);

  const handleDelete = (id: string): Promise<void> => {
    if (id) {
      setLoading(true);

      return cargoListStore
        .deleteCargo(selfStore.orgId, id)
        .then(() => {
          setModalVisible(false);
          refetch();
        })
        .finally(() => {
          setLoading(false);
        });
    }
  };

  return (
    <Modal
      centered
      title="Удалить груз"
      okText="Удалить"
      cancelText="Отменить"
      open={isModalVisible}
      closeIcon={<CloseIcon />}
      okButtonProps={{ loading: isLoading, danger: true }}
      cancelButtonProps={{ type: 'text' }}
      onOk={() => handleDelete(cargoListStore.cargoId)}
      onCancel={() => setModalVisible(false)}
    >
      <p>Вы уверены, что хотите удалить груз?</p>
    </Modal>
  );
});

export default DeleteCargoModal;
