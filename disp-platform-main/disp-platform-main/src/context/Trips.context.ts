import { useEffect } from 'react';
import { createCallableCtx } from 'utils/createCallableContext';
import { useAppStore } from 'ioc';
import { useTranslation } from 'i18n';
import { updateQueryCache, useAPIQueryCache } from 'api';
import {
  PASS_TRIPS_KEY, PASS_TRIP_KEY, TRIPS_STATISTIC_KEY, useTripsWebsocket
} from 'api/trips/trips.api';
import { useProfile } from 'api/profile/profile.api';

const useHook = () => {
  const websocket = useTripsWebsocket();

  const { contractorId } = useProfile().data;

  const { logger } = useAppStore();
  const { t } = useTranslation();

  const cache = useAPIQueryCache();

  useEffect(() => {
    if (!websocket.lastMessage?.data) {
      return;
    }

    // TODO: обсудить с ВП, как и куда должны добавляться заявки, полученные по вебсокетам.
    // Пока просто инвалидируем кэш, чтобы обновилась таблица
    cache.refetchQueries([PASS_TRIPS_KEY]);
    cache.refetchQueries([TRIPS_STATISTIC_KEY]);
    updateQueryCache(
      cache,
      [PASS_TRIP_KEY, contractorId, websocket.lastMessage.data.id],
      () => websocket.lastMessage!.data
    );

    if (websocket.lastMessage.data.isNew) {
      logger.toNotify('success', '', `${t.Requests.newTrip} ${websocket.lastMessage.data.humanReadableId}`, 0);
    } else {
      logger.toNotify('info', '', `${t.Requests.tripChanged} ${websocket.lastMessage.data.humanReadableId}`, 0);
    }
  }, [websocket.lastMessage, logger, t, cache, contractorId]);

  return websocket;
};

export const [useTripsContext, TripsProvider] = createCallableCtx(useHook, { name: 'TripsProvider' });
