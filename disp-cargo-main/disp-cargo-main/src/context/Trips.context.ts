import { useEffect } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';
import { useAppStore } from 'ioc';
import { useTranslation } from 'i18n';
import { useAPIQueryCache } from 'api';
import { useProfile } from 'api/profile/profile.api';
import { useCargoTripsWebsocket } from 'api/trips-cargo/trips-cargo.api';
import { CARGO_TRIPS_KEY, CARGO_TRIPS_STATISTIC_KEY } from 'api/trips-cargo/trips-cargo.constants';

const useHook = () => {
  const websocket = useCargoTripsWebsocket();

  const { contractorId } = useProfile().data;

  const { logger } = useAppStore();
  const { t } = useTranslation();

  const cache = useAPIQueryCache();

  useEffect(() => {
    if (!websocket.lastMessage) {
      return;
    }

    // TODO: обсудить с ВП, как и куда должны добавляться заявки, полученные по вебсокетам.
    // Пока просто инвалидируем кэш, чтобы обновилась таблица
    cache.invalidateQueries([CARGO_TRIPS_KEY]);
    cache.invalidateQueries([CARGO_TRIPS_STATISTIC_KEY]);
    cache.invalidateQueries([CARGO_TRIPS_KEY, contractorId, websocket.lastMessage.data.id]);

    if (websocket.lastMessage.data.isNew) {
      logger.toNotify('success', '', `${t.Requests.newTrip} ${websocket.lastMessage.data.humanReadableId}`, 0);
    } else {
      logger.toNotify('info', '', `${t.Requests.tripChanged} ${websocket.lastMessage.data.humanReadableId}`, 0);
    }
  }, [websocket.lastMessage, logger, t, cache, contractorId]);

  return websocket;
};

export const [useTripsContext, TripsProvider] = createCallableCtx(useHook, { name: 'TripsProvider' });
