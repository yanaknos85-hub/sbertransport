import React, { useEffect, useState } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';
import { SpinWrapped } from 'shared/components';
import { CargoLayout, CargoLayoutWrapper } from 'shared/components/Cargo/CargoLayout';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';
import { UUID } from 'utils/io-ts';

import CargoRequestContent from './CargoRequestContent';

const CargoDetailed: React.FC<{
  isApproval: boolean;
  isRegular?: boolean;
  isJournal: boolean;
}> = observer(({
  isApproval, isRegular = false, isJournal,
}) => {
  const match = useRouteMatch<{ reqId: UUID }>();
  const { reqId } = match.params;
  const { [StoreNames.cargosStore]: cargosStore, logger } = useAppStoreContext();
  const [preloader, setPreloader] = useState(true);

  const getRequest = async () => {
    setPreloader(true);
    try {
      await cargosStore.getMultipleRequestById(reqId);
      await cargosStore.getMultipleStatusHistoryById(reqId);
    } catch (error) {
      logger.toError('Ошибка загрузки', 'Попробуйте перезагрузить страницу');
    } finally {
      setPreloader(false);
    }
  };

  const getRequestRegular = async () => {
    setPreloader(true);
    try {
      await cargosStore.getMultipleRegularRequestById(reqId);
      await cargosStore.getMultipleRegularStatusHistoryById(reqId);
    } catch (error) {
      logger.toError('Ошибка загрузки', 'Попробуйте перезагрузить страницу');
    } finally {
      setPreloader(false);
    }
  };

  useEffect(() => {
    if (reqId) {
      if (isRegular) {
        getRequestRegular();
      } else {
        getRequest();
      }
    }
  }, [reqId, cargosStore]);

  if (preloader) {
    return <SpinWrapped />;
  }

  const request = isRegular ? cargosStore.cargoMultipleRegularRequest : cargosStore.cargoMultipleRequest;
  const history = isRegular ? cargosStore.cargoMultipleRegularHistory : cargosStore.cargoMultipleHistory;

  return (
    <CargoLayoutWrapper>
      <CargoLayout>
        {request ? (
          <CargoRequestContent
            request={request}
            history={history}
            isApproval={isApproval}
            isRegular={isRegular}
            isJournal={isJournal}
          />
        ) : (
          <SpinWrapped />
        )}
      </CargoLayout>
    </CargoLayoutWrapper>
  );
});

export default CargoDetailed;
