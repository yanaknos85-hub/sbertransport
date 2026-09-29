import React, { useMemo, FC } from 'react';
import { Modal } from 'antd';
import { observer } from 'mobx-react';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { ReactComponent as CloseIcon } from 'components/Evaluation/static/icons/closeIcon.svg';
import { StoreNames } from 'stores/StoreNames.enum';

import { DelegatesModalTypes, DelegatesTexts, DelegatesTextsCyrillic } from '../../Delegates.constants';
import { useModal } from '../../context/modal.context';
import { useDelegates } from '../../hooks/useDelegateForm';
import DelegateAddEditForm from '../DelegateAddEditForm/DelegateAddEditForm';

import styles from './DelegateAddEditModal.module.scss';

interface DelegateAddEditProps {
  refetch: () => void;
}

const DelegateAddEditModal: FC<DelegateAddEditProps> = observer(({ refetch }) => {
  const {
    [StoreNames.delegatesStore]: delegatesStore,
    [StoreNames.transportTypesStore]: transportTypesStore,
    [StoreNames.selfStore]: selfEmployee,
  } = useAppStoreContext();
  const { modalState, closeModal } = useModal();

  const modalTitle = useMemo(
    () => DelegatesTextsCyrillic[
      DelegatesTexts[
        modalState.type === DelegatesModalTypes.edit ? DelegatesTexts.edit : DelegatesTexts.add
      ]
    ],
    [modalState.type]
  );

  const {
    onFinish, form, onValuesChange, isLoading, isFormDirty, setFormDirty,
  } = useDelegates({
    delegatesStore,
    transportTypesStore,
    selfEmployee,
    refetch,
  });

  const handleClose = () => {
    closeModal();
    form.resetFields();
    setFormDirty(false);
    transportTypesStore.clearActiveTransportType();
  };

  const delegate
    = modalState.type === DelegatesModalTypes.edit
      ? delegatesStore.delegates.find(({ id }) => id === modalState.id)
      : null;

  return (
    <Modal
      centered
      open={modalState.type === DelegatesModalTypes.add || modalState.type === DelegatesModalTypes.edit}
      title={modalTitle}
      okText={DelegatesTextsCyrillic[DelegatesTexts.save]}
      cancelText={DelegatesTextsCyrillic[DelegatesTexts.cancel]}
      className={styles.modal}
      wrapClassName={styles.wrapModal}
      closeIcon={<CloseIcon />}
      okButtonProps={{
        htmlType: 'submit',
        loading: isLoading,
        disabled: !isFormDirty,
        onClick: () => form.submit(),
      }}
      cancelButtonProps={{ type: 'text' }}
      onOk={() => null}
      onCancel={handleClose}
      destroyOnClose
    >
      <DelegateAddEditForm
        form={form}
        delegate={delegate}
        onValuesChange={onValuesChange}
        onFinish={onFinish}
      />
    </Modal>
  );
});

export default DelegateAddEditModal;
