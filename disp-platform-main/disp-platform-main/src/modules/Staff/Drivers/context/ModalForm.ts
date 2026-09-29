import { useCallback, useState } from 'react';
import { Driver } from 'api/drivers/drivers.types';
import { createCallableCtx } from 'utils/createCallableContext';

const useHook = () => {
  const [stateShowModal, setStateShowModal] = useState<
    | {
      isOpen: boolean;
      type: 'add';
      driver?: never;
    }
    | {
      isOpen: boolean;
      type: 'edit';
      driver: Driver;
    }
  >({ isOpen: false, type: 'add' });

  const handleOpenAdd = useCallback(() => setStateShowModal({
    type: 'add', isOpen: true, driver: undefined,
  }), []);

  const handleOpenEdit = useCallback((driver: Driver) => {
    setStateShowModal({
      type: 'edit', isOpen: true, driver,
    });
  }, []);

  const handleClose = useCallback(() => setStateShowModal(state => ({ ...state, isOpen: false })), []);

  return {
    stateShowModal,
    handleOpenAdd,
    handleOpenEdit,
    handleClose,
  };
};

export const [useModalForm, ModalFormProvider] = createCallableCtx(useHook, { name: 'ModalFormProvider' });
