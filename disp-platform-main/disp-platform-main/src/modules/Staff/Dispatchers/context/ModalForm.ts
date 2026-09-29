import { useCallback, useState } from 'react';
import { ContractorDispatcher } from 'api/dispatchers/dispatchers.types';
import { createCallableCtx } from 'utils/createCallableContext';

const useHook = () => {
  const [stateShowModal, setStateShowModal] = useState<
    | {
      isOpen: boolean;
      type: 'add';
      dispatcher?: never;
    }
    | {
      isOpen: boolean;
      type: 'edit';
      dispatcher: ContractorDispatcher;
    }
  >({ isOpen: false, type: 'add' });

  const handleOpenAdd = useCallback(
    () => setStateShowModal({
      type: 'add',
      isOpen: true,
      dispatcher: undefined,
    }),
    []
  );

  const handleOpenEdit = useCallback((dispatcher: ContractorDispatcher) => {
    setStateShowModal({
      type: 'edit',
      isOpen: true,
      dispatcher,
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
