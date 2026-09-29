/* eslint-disable no-nested-ternary */
import React, { useEffect, useState } from 'react';
import cn from 'classnames';

import styles from './modal.module.scss';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import './item.scss';

interface Props {
  addresses: WaypointModel[];
  className?: string;
}

function RouteMap({ addresses, className }: Props): JSX.Element {
  const [openRoute, setOpenRout] = useState(false);
  const defoultRoute = [addresses[0], addresses[addresses.length - 1]];
  const [hiddenAddresses, setHiddenAddresses] = useState<WaypointModel[]>([]);

  useEffect(() => {
    setHiddenAddresses(defoultRoute);
  }, [addresses]);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setOpenRout(!openRoute);
    event.stopPropagation();
  };

  useEffect(() => {
    // eslint-disable-next-line no-unused-expressions
    openRoute ? setHiddenAddresses(addresses) : setHiddenAddresses(defoultRoute);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [openRoute]);

  return (
    <div className={cn(styles.wrapperMain, className)} onClick={event => handleClick(event)}>
      {hiddenAddresses?.map((address, index) => (
        <div key={index} className={styles.wrapperAddresses}>
          <div
            // eslint-disable-next-line no-nested-ternary
            className={
              index === 0
                ? styles.startMarker
                : index === hiddenAddresses.length - 1
                  ? styles.endMarker
                  : styles.stopMarker
            }
          >
            {openRoute
              ? String.fromCharCode(65 + index)
              : index === 0 ? String.fromCharCode(65 + index) : String.fromCharCode(65 + addresses.length - 1)}
          </div>
          <div>
            {index === 0 ? (
              <div className={styles.routeTitle}>
                <span>Начало поездки</span>
                <span>{address?.addressStringWithRegion}</span>
              </div>
            ) : index === hiddenAddresses.length - 1 ? (
              <div className={styles.routeTitle}>
                <span>Конец поездки</span>
                <span>{address?.addressStringWithRegion}</span>
              </div>
            ) : (
              <div className={styles.routeTitle}>
                <span>
                  Остановка №
                  {index}
                </span>
                <span>{address?.addressStringWithRegion}</span>
              </div>
            )}
          </div>
          {index < hiddenAddresses.length - 1 && (
            <div
              className={
                addresses.length > 2
                  ? index === 0
                    ? styles.startLine
                    : index === addresses.length - 2
                      ? styles.endLine
                      : styles.stopLine
                  : styles.LineNonStop
              }
            />
          )}
        </div>
      ))}
    </div>
  );
}

export default RouteMap;
