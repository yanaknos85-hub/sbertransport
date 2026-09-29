import { useCallback, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';

import { useSelfAutopark } from 'api/contractors/contractors.api';
import { Vehicle } from 'api/vehicles/vehicles.types';
import { Transport } from 'api/transport/transport.types';
import { createCallableCtx } from 'utils/createCallableContext';

const useHook = () => {
  const history = useHistory();
  const { routeId } = useParams<{ routeId: string }>();

  const { isInternal } = useSelfAutopark().data;

  const [stateShowModal, setStateShowModal] = useState<
    | {
      isOpen: boolean;
      type: 'add';
      vehicle?: never;
    }
    | {
      isOpen: boolean;
      type: 'edit';
      vehicle: Vehicle;
    }
    | {
      isOpen: boolean;
      type: 'upload';
      vehicle?: never;
    }
    | {
      isOpen: boolean;
      type: 'result';
      vehicle?: never;
    }
    | {
      isOpen: boolean;
      type: 'addTransport';
      transport?: never;
      vehicle?: never;
    }
    | {
      isOpen: boolean;
      type: 'editTransport';
      transport: Transport;
      vehicle?: never;
    }
    | {
      isOpen: boolean;
      type: 'deleteTransport';
      transport?: never;
      vehicle?: never;
    }
  >({ isOpen: routeId === 'add', type: isInternal ? 'addTransport' : 'add' });

  const handleOpenAdd = useCallback(() => setStateShowModal({
    type: 'add', isOpen: true, vehicle: undefined,
  }), []);

  const handleOpenEdit = useCallback((vehicle: Vehicle) => {
    setStateShowModal({
      type: 'edit', isOpen: true, vehicle,
    });
  }, []);

  const handleOpenAddTransport = useCallback(() => setStateShowModal({
    type: 'addTransport', isOpen: true, transport: undefined,
  }), []);

  const handleOpenEditTransport = useCallback((transport: Transport) => {
    setStateShowModal({
      type: 'editTransport', isOpen: true, transport,
    });
  }, []);

  const handleOpenDeleteTransport = useCallback(() => {
    setStateShowModal({
      type: 'deleteTransport', isOpen: true,
    });
  }, []);

  const handleOpenUpload = useCallback(() => setStateShowModal({
    type: 'upload', isOpen: true, vehicle: undefined,
  }), []);

  const handleOpenResult = useCallback(() => setStateShowModal({
    type: 'result', isOpen: true, vehicle: undefined,
  }), []);

  const handleClose = useCallback(() => {
    history.push({
      ...history.location,
      pathname: history.location.pathname.replace(`/${routeId}`, ''),
    });

    setStateShowModal(state => ({ ...state, isOpen: false }));
  }, [history, routeId]);

  return {
    stateShowModal,
    handleOpenAdd,
    handleOpenEdit,
    handleOpenAddTransport,
    handleOpenEditTransport,
    handleOpenDeleteTransport,
    handleOpenUpload,
    handleOpenResult,
    handleClose,
  };
};

export const [useModalForm, ModalFormProvider] = createCallableCtx(useHook, { name: 'ModalFormProvider' });
