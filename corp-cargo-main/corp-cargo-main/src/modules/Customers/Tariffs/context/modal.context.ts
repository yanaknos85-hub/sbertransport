/* eslint-disable no-unused-vars, @typescript-eslint/no-unused-vars */
import { useCallback, useState } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';
import { useParams, useLocation } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import { RouteIds } from 'modules/TariffSettings/constants';

type ModalState =
  | {
    type: null;
  }
  | {
    type: 'add';
  }
  | {
    type: 'edit';
    id: string;
    transportType: string;
  }
  | {
    type: 'filters';
  };

const getType = (routeId: string): ModalState['type'] => {
  switch (routeId) {
    case RouteIds.Add:
      return 'add';
    case RouteIds.All:
      return null;
    default:
      return 'edit';
  }
};

const useHook = () => {
  const history = useHistory();
  const location = useLocation();
  const { routeId, transportType } = useParams<{ routeId: string; transportType: string }>();
  const test = useParams();
  const [modalState, setModalState] = useState<ModalState>({
    type: getType(routeId), id: routeId, transportType,
  });

  const openAdd = useCallback(() => {
    setModalState({ type: 'add' });
    history.push(location.pathname.replace(routeId, RouteIds.Add));
  }, [location, routeId, history]);

  const openEdit = useCallback(
    (id: string, type: string) => {
      setModalState({
        type: 'edit', id, transportType: type,
      });
      history.push(`${location.pathname.replace(routeId, id)}/${type}`);
    },
    [location, routeId, history]
  );

  const openFilters = useCallback(() => {
    setModalState({ type: 'filters' });
  }, []);

  const closeModal = useCallback(() => {
    setModalState({ type: null });
    history.push(`${location.pathname.split(routeId)[0]}${RouteIds.All}`);
  }, [location, routeId, history]);

  return {
    modalState,
    openAdd,
    openEdit,
    openFilters,
    closeModal,
  };
};

export const [useModal, ModalProvider] = createCallableCtx(useHook, { name: 'ModalProvider' });
