import { useQueryClient } from '@tanstack/react-query';
import { TripsKeys, useTripsWebsocket } from 'api/services/Trips/Trips.query';
import { PassTrip, PassTrips } from 'api/services/Trips/Trips.types';
import { activeTripStatuses, finalTripStatuses, TRIP_STATUSES } from 'constants/trips.constants';
import { useEffect } from 'react';
import { useAppStore } from 'stores/stores.context';

export const useTripsUpdate = () => {
  const client = useQueryClient();

  const { lastMessage } = useTripsWebsocket();

  const { mapStore } = useAppStore();

  useEffect(() => {
    if (lastMessage) {
      // Обновляем данные об этой поездке
      client.setQueryData([TripsKeys.Trip, lastMessage.data.id], lastMessage.data);

      // Обновляем данные о текущей поездке, если совпадают id
      client.setQueryData(
        [TripsKeys.CurrentTrip],
        (prev?: PassTrip) => {
          if (prev?.id === lastMessage.data.id) {
            // Если поездка завершена, рефетчим текущую поездку, т.к. вручную проставить undefined невозможно
            // + очищаем карту
            if (lastMessage.data.status == TRIP_STATUSES.ORDER_FINISHED) {
              client.invalidateQueries({ queryKey: [TripsKeys.CurrentTrip] });
              mapStore.clear();
            } else {
              return lastMessage.data;
            }
          }

          return undefined;
        }
      );

      // Добавляем вверх списка поездку, если она новая
      if (lastMessage.data.isNew) {
        client.setQueryData(
          [TripsKeys.Trips, { statuses: [TRIP_STATUSES.DRIVER_ASSIGNED] }],
          (prev?: PassTrips) => prev && ({
            ...prev,
            numberOfElements: prev.numberOfElements + 1,
            totalElements: prev.totalElements + 1,
            content: [
              lastMessage.data,
              ...prev.content,
            ],
          })
        );

        client.invalidateQueries({ queryKey: [TripsKeys.Trips, { statuses: activeTripStatuses }] });
      } else {
        const isStartTrip = lastMessage.data.status === TRIP_STATUSES.DRIVER_ON_THE_WAY;

        // Если у нас не было поездки, а пришедшая поездка имеет статус DRIVER_ON_THE_WAY, значит мы выезжаем по заявке. Обновляем данные о текущей поездке
        if (!client.getQueryData([TripsKeys.CurrentTrip]) && isStartTrip) {
          client.setQueryData(
            [TripsKeys.CurrentTrip],
            lastMessage.data
          );
        }

        if (finalTripStatuses.includes(lastMessage.data.status)) {
          client.invalidateQueries({ queryKey: [TripsKeys.Trips, { statuses: finalTripStatuses }] });
        }
      }
    }
  }, [client, lastMessage, mapStore]);
};
