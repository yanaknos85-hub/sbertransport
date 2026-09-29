import { useState } from 'react';
import { AutoPark } from 'api/autopark/autopark.types';
import { createCallableCtx } from 'utils/createCallableContext';

const useHook = () => {
  const [stateShowModal, setStateShowModal] = useState<
    | {
      isOpen: boolean;
      type: 'add';
      autopark?: never;
    }
    | {
      isOpen: boolean;
      type: 'edit';
      autopark: AutoPark;
    }
  >({ isOpen: false, type: 'add' });

  const handleOpenAdd = () => setStateShowModal({
    type: 'add', isOpen: true, autopark: undefined,
  });

  const handleOpenEdit = (autopark: AutoPark) => {
    setStateShowModal({
      type: 'edit', isOpen: true, autopark,
    });
  };

  const handleClose = () => setStateShowModal(state => ({ ...state, isOpen: false }));

  return {
    stateShowModal,
    handleOpenAdd,
    handleOpenEdit,
    handleClose,
  };
};

export const [useModalForm, ModalFormProvider] = createCallableCtx(useHook, { name: 'ModalFormProvider' });
