import React, { FC } from 'react';
import { useMapType } from 'shared/hooks/useMapType';

import { MapComponentType } from 'constants/constants.app';

import { MapComponent as Map2GIS } from './Map2GIS/Map2GIS';
import { IMapComponentProps, Padding } from './MapComponent.types';
import { MapComponent as MapOL } from './MapOpenlayers/MapOL';

export const DEFAULT_PADDING: Padding = {
  top: 20,
  bottom: 20,
  left: 20,
  right: 20,
};

export const MapComponent: FC<IMapComponentProps> = props => {
  const mapType = useMapType();
  return (
    <div className={props.className}>
      {mapType === MapComponentType.webGL2GIS && <Map2GIS {...props} />}
      {mapType === MapComponentType.openlayers && <MapOL {...props} />}
    </div>
  );
};
