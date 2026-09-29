import { useState, useCallback } from 'react';

import { useHistory } from '@sber-sbertransport/mf-core';

import { useParams } from 'react-router-dom';

import { ShiftConflict } from 'api/shift-conflicts/shift-conflicts.types';

import { createCallableCtx } from 'utils/createCallableContext';

import { useSelectedShift } from './selectedShift.context';

const useHook = () => {
  const history = useHistory();
  const { routeId } = useParams<{ routeId: string }>();

  const [modal, setModal] = useState<'create' | 'delete' | 'filters' | 'order' | 'settings' | 'deleteConflict' | null>(null);
  const { setConflictShift } = useSelectedShift();

  const handleOpenCreate = useCallback(() => setModal('create'), []);

  const handleOpenDelete = useCallback(() => setModal('delete'), []);

  const handleOpenFilters = useCallback(() => setModal('filters'), []);

  const handleOpenOrder = useCallback(() => setModal('order'), []);

  const handleOpenSettings = useCallback(() => setModal('settings'), []);

  const handleOpenDeleteConflict = useCallback((conflict: ShiftConflict) => {
    setConflictShift(conflict);
    setModal('deleteConflict');
  }, [setConflictShift]);

  const handleClose = useCallback(() => {
    setConflictShift(null);

    history.push({
      ...history.location,
      pathname: history.location.pathname.replace(`/${routeId}`, ''),
    });

    setModal(null);
  }, [history, routeId, setConflictShift]);

  return {
    isCreateOpened: modal === 'create' || routeId === 'add',
    isDeleteOpened: modal === 'delete',
    isFiltersOpened: modal === 'filters',
    isOrderOpened: modal === 'order',
    isSettingsOpened: modal === 'settings',
    isDeleteConflictOpened: modal === 'deleteConflict',
    handleOpenCreate,
    handleOpenDelete,
    handleOpenFilters,
    handleOpenOrder,
    handleOpenSettings,
    handleOpenDeleteConflict,
    handleClose,
  };
};

export const [useModals, ModalsProvider] = createCallableCtx(useHook, { name: 'ModalsProvider' });
