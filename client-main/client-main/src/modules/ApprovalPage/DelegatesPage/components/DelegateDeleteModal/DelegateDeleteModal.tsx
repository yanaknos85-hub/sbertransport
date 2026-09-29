import React, { useState, FC } from 'react';
import Modal from 'antd/lib/modal';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { ReactComponent as CloseIcon } from 'components/Evaluation/static/icons/closeIcon.svg';
import { StoreNames } from 'stores/StoreNames.enum';

import { DelegatesModalTypes, DelegatesTexts, DelegatesTextsCyrillic } from '../../Delegates.constants';
import { useModal } from '../../context/modal.context';
import styles from './DelegateDeleteModal.module.scss';

interface DelegateDeleteProps {
  refetch: () => void;
}

const DelegateDeleteModal: FC<DelegateDeleteProps> = ({ refetch }) => {
  const { [StoreNames.delegatesStore]: delegatesStore } = useAppStoreContext();
  const { modalState, closeModal } = useModal();
  const [isLoading, setLoading] = useState(false);

  const handleDelete = (id?: string): Promise<void> => {
    if (id) {
      setLoading(true);

      return delegatesStore
        .deleteDelegate(id)
        .then(() => {
          closeModal();
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
      open={modalState.type === DelegatesModalTypes.delete}
      title={DelegatesTextsCyrillic[DelegatesTexts.remove]}
      okText={DelegatesTextsCyrillic[DelegatesTexts.remove]}
      cancelText={DelegatesTextsCyrillic[DelegatesTexts.cancel]}
      className={styles.modal}
      closeIcon={<CloseIcon />}
      okButtonProps={{ loading: isLoading, danger: true }}
      cancelButtonProps={{ type: 'text' }}
      onOk={() => handleDelete(modalState.id)}
      onCancel={closeModal}
    >
      <p>{DelegatesTextsCyrillic[DelegatesTexts.deleteConfirm]}</p>
    </Modal>
  );
};

export default DelegateDeleteModal;
