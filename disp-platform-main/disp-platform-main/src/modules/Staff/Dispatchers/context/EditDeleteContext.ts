import { useCallback } from 'react';

import { useProfile } from 'api/profile/profile.api';
import { ContractorDispatcher } from 'api/dispatchers/dispatchers.types';
import { useDeleteDispatcher } from 'api/dispatchers/dispatchers.api';

import { createCallableCtx } from 'utils/createCallableContext';
import { UUID } from 'utils/io-ts';
import { useModalForm } from './ModalForm';

const useHook = () => {
  const { handleOpenEdit } = useModalForm();

  const { contractorId } = useProfile().data;

  const [deleteDispatcher] = useDeleteDispatcher({ contractorId });

  const handleEditClick = useCallback(
    (selectedDispatcher: ContractorDispatcher) => {
      handleOpenEdit(selectedDispatcher);
    },
    [handleOpenEdit]
  );

  const handleDeleteClick = useCallback(
    (id: UUID) => deleteDispatcher({ dispId: id }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [deleteDispatcher, contractorId]
  );

  return {
    handleEditClick,
    handleDeleteClick,
  };
};

export const [useEditDelete, EditDeleteProvider] = createCallableCtx(useHook, {
  name: 'EditDeleteProvider',
});
