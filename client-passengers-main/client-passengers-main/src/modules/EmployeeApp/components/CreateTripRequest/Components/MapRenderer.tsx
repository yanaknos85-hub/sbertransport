import React, { FC } from 'react';
import { observer } from 'mobx-react';

import { MapComponent } from 'shared/components/Map/MapComponent';
import { IMapComponentProps } from 'shared/components/Map/MapComponent.types';

import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import styles from '../styles/create.module.scss';

interface Props extends IMapComponentProps {
  geo: any;
}

export const MapRenderer: FC<Props> = observer(props => {
  const {
    geo, zoomControlPosition, onCenterChanged,
  } = props;
  const { [StoreNames.geoStore]: geoStore, logger } = useAppStoreContext();

  const onCoordinatesClick = ({ longitude, latitude }: { latitude: number; longitude: number }) => {
    geoStore.onMapClick(latitude, longitude, logger);
  };
  const position = {
    latitude: geo.currentCoordinates[0],
    longitude: geo.currentCoordinates[1],
  };

  return (
    <MapComponent
      position={position}
      markers={geo.waypoints.filter(point => point.isValid)}
      polylines={geo.calculatedRoute?.segments}
      className={styles.map}
      dragging={true}
      zoomControl={true}
      zoomControlPosition={zoomControlPosition}
      onCenterChanged={onCenterChanged}
      onCoordinatesClick={onCoordinatesClick}
      boundsPadding={{
        top: 40, bottom: 40, left: 40, right: 400,
      }}
    />
  );
});
