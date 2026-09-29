import { useCallback, useState } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';
import { useHistory, useParams } from 'react-router-dom';

export enum RouteIds {
  All = 'all',
  Add = 'add',
}

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
  const { routeId } = useParams<{ routeId: string }>();

  const [modalState, setModalState] = useState<ModalState>({ type: getType(routeId), id: routeId });

  const openAdd = useCallback(() => {
    setModalState({ type: 'add' });
    history.push({
      ...history.location,
      pathname: history.location.pathname.replace(routeId, RouteIds.Add),
    });
  }, [history, routeId]);

  const openEdit = useCallback(
    (id: string) => {
      setModalState({ type: 'edit', id });
      history.push({
        ...history.location,
        pathname: history.location.pathname.replace(routeId, id),
      });
    },
    [history, routeId]
  );

  const openFilters = useCallback(() => {
    setModalState({ type: 'filters' });
  }, []);

  const closeModal = useCallback(() => {
    setModalState({ type: null });
    history.push({
      ...history.location,
      pathname: history.location.pathname.replace(routeId, RouteIds.All),
    });
  }, [history, routeId]);

  return {
    modalState,
    openAdd,
    openEdit,
    openFilters,
    closeModal,
  };
};

export const [useModal, ModalProvider] = createCallableCtx(useHook, { name: 'ModalProvider' });
