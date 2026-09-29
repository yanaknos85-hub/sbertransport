import React, { FC, Suspense } from 'react';
import {
  DEFAULT_ZOOM, Map2GIS, MapPointerEvent,
  Marker2GIS, MOSCOW,
  Polyline2GIS
} from '@sber-sbertransport/ui-kit/src';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import SpinWrapped from 'shared/components/SpinWrapped';

import uuid from 'utils/uuid';

import markerIcon from '../images/marker-icon.png';
import { DEFAULT_PADDING } from '../MapComponent';
import { IMapComponentProps } from '../MapComponent.types';
import { use2GISBounds } from './use2GISBounds';

const GisControlPosition = {
  bottomright: 'bottomRight',
  topleft: 'topLeft',
  topright: 'topRight',
  bottomleft: 'bottomLeft',
};// для перевода опций лифлета в опции 2гис

export type T2GisControlPosition = boolean | undefined | 'topLeft' | 'topCenter' | 'topRight' | 'centerLeft'
  | 'centerRight' | 'bottomLeft' | 'bottomCenter' | 'bottomRight';

export interface Viewport {
  center: [number, number] | null | undefined;
  zoom: number | null | undefined;
}
export type CenterCoordinates = Viewport['center'];

export const MapComponent: FC<IMapComponentProps> = props => {
  const {
    position = undefined,
    markers = [],
    polylines = [],
    dragging = false,
    zoomControl = false,
    zoomControlPosition = 'bottomleft',
    onClick,
    onCoordinatesClick,
    onCenterChanged,
    boundsPadding = DEFAULT_PADDING,
  } = props;

  const bounds = use2GISBounds({ markers, polylines });
  const centerPosition = bounds ? [
    (bounds.northEast[0] + bounds.southWest[0]) * 0.5,
    (bounds.northEast[1] + bounds.southWest[1]) * 0.5,
  ] : position ? [position.longitude, position.latitude] : MOSCOW;

  const zoomControlPosition2GIS = zoomControlPosition in GisControlPosition
    ? GisControlPosition[zoomControlPosition]
    : 'bottomLeft';

  const mapZoom = DEFAULT_ZOOM;

  const onMoveEnd = (coordinates: number[]) => {
    const [longitude, latitude] = coordinates;
    onCenterChanged && onCenterChanged({ longitude, latitude });
  };

  const onMapClick = (e: MapPointerEvent) => {
    const [longitude, latitude] = e.lngLat;
    onClick && onClick(e);
    onCoordinatesClick && onCoordinatesClick({ latitude, longitude });
  };

  return (
    <ErrorBoundary>
      <Suspense fallback={<SpinWrapped />}>
        <Map2GIS
          containerStyle={{ height: '100%', width: '100%' }}
          center={centerPosition}
          zoom={mapZoom}
          disableDragging={!dragging}
          fullScreenControl={false}
          zoomControl={zoomControl ? zoomControlPosition2GIS as T2GisControlPosition : false}
          copyright="bottomCenter"
          bounds={bounds}
          onClick={onMapClick}
          onCenterChange={onMoveEnd}
          boundsPadding={boundsPadding}
        >
          {markers && markers.map(marker => (
            <Marker2GIS
              icon={markerIcon}
              rotation={undefined}
              key={uuid()}
              coordinates={[marker.longitude as number, marker.latitude as number]}
              userData={marker}
              size={[24, 48]}
            />
          ))}

          {polylines && polylines.map(segment => (
            <Polyline2GIS
              key={uuid()}
              coordinates={segment.coordinates.map(coords => ([coords.longitude, coords.latitude])) as number[][]}
            />
          ))}

        </Map2GIS>
      </Suspense>
    </ErrorBoundary>
  );
};
