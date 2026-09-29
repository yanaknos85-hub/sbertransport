import { useCallback } from 'react';
import { Driver } from 'api/drivers/drivers.types';
import { createCallableCtx } from 'utils/createCallableContext';
import { useModalForm } from './ModalForm';

const useHook = () => {
  const { handleOpenEdit } = useModalForm();

  const handleEditClick = useCallback((selectedDriver: Driver) => {
    handleOpenEdit(selectedDriver);
  }, [handleOpenEdit]);

  return {
    handleEditClick,
  };
};

export const [useEditDelete, EditDeleteProvider] = createCallableCtx(useHook, {
  name: 'EditDeleteProvider',
});
