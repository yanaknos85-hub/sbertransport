import { useCallback } from 'react';
import { Vehicle } from 'api/vehicles/vehicles.types';
import { createCallableCtx } from 'utils/createCallableContext';
import { useModalForm } from './ModalForm';

const useHook = () => {
  const { handleOpenEdit } = useModalForm();

  const handleEditClick = useCallback((selectedVehicle: Vehicle) => {
    handleOpenEdit(selectedVehicle);
  }, [handleOpenEdit]);

  return {
    handleEditClick,
  };
};

export const [useEditDelete, EditDeleteProvider] = createCallableCtx(useHook, {
  name: 'EditDeleteProvider',
});
