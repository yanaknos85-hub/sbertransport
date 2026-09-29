import { useCallback, useState } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';
import { useHistory, useParams, useLocation } from 'react-router-dom';
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
  const { routeId } = useParams<{ routeId: string }>();

  const [modalState, setModalState] = useState<ModalState>({ type: getType(routeId), id: routeId });

  const openAdd = useCallback(() => {
    setModalState({ type: 'add' });
    history.push(location.pathname.replace(routeId, RouteIds.Add));
  }, [location, routeId, history]);

  const openEdit = useCallback(
    (id: string) => {
      setModalState({ type: 'edit', id });
      history.push(location.pathname.replace(routeId, id));
    },
    [location, routeId, history]
  );

  const openFilters = useCallback(() => {
    setModalState({ type: 'filters' });
  }, []);

  const closeModal = useCallback(() => {
    setModalState({ type: null });
    history.push(location.pathname.replace(routeId, RouteIds.All));
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
