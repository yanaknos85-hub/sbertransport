import { useCallback, useState } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';
import { DelegatesModalTypes } from '../Delegates.constants';

interface ModalState {
  type: DelegatesModalTypes | null;
  id?: string;
}

const useHook = () => {
  const [modalState, setModalState] = useState<ModalState>({ type: null });

  const openAdd = useCallback(() => {
    setModalState({ type: DelegatesModalTypes.add });
  }, []);

  const openEdit = useCallback((id: string) => {
    setModalState({ type: DelegatesModalTypes.edit, id });
  }, []);

  const openDelete = useCallback((id: string) => {
    setModalState({ type: DelegatesModalTypes.delete, id });
  }, []);

  const closeModal = useCallback(() => {
    setModalState({ type: null });
  }, []);

  return {
    modalState,
    openAdd,
    openEdit,
    openDelete,
    closeModal,
  };
};

export const [useModal, ModalProvider] = createCallableCtx(useHook, { name: 'ModalProvider' });
