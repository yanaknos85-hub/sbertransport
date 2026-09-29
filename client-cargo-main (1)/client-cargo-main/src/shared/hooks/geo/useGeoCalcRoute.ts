import { useEffect, useState } from 'react';
import { RouteModel } from 'shared/models/geo/Route.model';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { useCalcRoute } from 'api/geo';
import { waypointsLocationIsSimilar } from 'utils/waypoint';
import { allWaypointsIsValid, MIN_WAYPOINT_LENGTH } from 'utils/waypoint/core';

interface HookProps {
  initRoute?: RouteModel;
  fetchOnInit?: boolean;
  waypoints: WaypointModel[];
  prevWaypoints: WaypointModel[] | undefined;
}

/**
 * Хук, который рассчитывает путь на основе waypoints.
 * По умолчанию рассчитывает путь, только в случае изменения
 * длины waypoints(при вызове метода addWaypoint или removeWaypoint),
 * либо с включенным флагом fetchOnInit.
 */
const useGeoCalcRoute = ({
  fetchOnInit,
  waypoints,
  prevWaypoints,
  initRoute,
}: HookProps): {
  inProgress: boolean;
  calculatedRoute: RouteModel | undefined;
  stateReady: boolean;
} => {
  const [calculatedRoute, _setCalculatedRoute] = useState<RouteModel | undefined>(initRoute);
  // FIXME придумать более изящное решение
  // Приходится использовать флаг stateActual, так как данные в состоянии обновляются не синхронно.
  // Можно было бы переписать хук на возвращение data из useCalcRoute, тогда не было бы
  // проблем с возвращением не консистентного состояния. Но этому мешает текущая реализация бэка, так как
  // не возвращаются некоторые нужные поля (пример: absenceReason, checkinManual, checkinAutomatic)
  const [stateActual, setStateActual] = useState(false);

  const waypointsIsCorrect = waypoints.length >= MIN_WAYPOINT_LENGTH && allWaypointsIsValid(waypoints);

  const triggerCalculateManually = fetchOnInit && waypointsIsCorrect;
  const triggerCalculateAutomatically
    = waypointsIsCorrect
    && !waypointsLocationIsSimilar(prevWaypoints, waypoints)
    && prevWaypoints !== undefined
    && prevWaypoints.length !== 0;

  const checkCalcRequestEnabled = triggerCalculateManually || triggerCalculateAutomatically;

  const {
    data, isFetching, isLoading, isFetched,
  } = useCalcRoute(
    { waypoints, previousRoute: calculatedRoute },
    { enabled: checkCalcRequestEnabled, suspense: false }
  );

  useEffect(() => {
    _setCalculatedRoute(initRoute);
  }, [initRoute]);

  useEffect(() => {
    if (!isFetched) {
      setStateActual(false);
    }
  }, [isFetched]);

  // update & init
  useEffect(() => {
    if (data && waypoints) {
      // Заменим waypoints из результата на те, которые были
      // получены изначально из заявки (с учётом изменений).
      // Так как бэк (в рамках этого метода) вернёт waypoints
      // без полей, которые нужны в рамках заявки
      // Пример: absenceReason, checkinManual, checkinAutomatic
      _setCalculatedRoute(new RouteModel({ ...data, waypoints }));
      setStateActual(true);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [data]);
  // FIXME react-hooks/exhaustive-deps

  // unmount
  useEffect(
    () => (): void => {
      _setCalculatedRoute(undefined);
    },
    []
  );

  const inProgress = isLoading || isFetching;
  const stateReady = !inProgress && isFetched && stateActual;

  return {
    inProgress,
    stateReady,
    calculatedRoute,
  };
};

export default useGeoCalcRoute;
