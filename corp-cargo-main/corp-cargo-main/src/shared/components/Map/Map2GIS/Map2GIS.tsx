import React, { FC, Suspense } from 'react';
import {
  DEFAULT_ZOOM, Map2GIS, Marker2GIS, MOSCOW,
  Polyline2GIS, MapPointerEvent
} from '@sber-sbertransport/ui-kit/src';

import uuid from 'utils/uuid';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';
import greenEllipse from 'shared/assets/svg/greenEllipse.svg';
import greenSmallEllipse from 'shared/assets/svg/greenSmallEllipse.svg';
import blueEllipse from 'shared/assets/svg/blueEllipse.svg';

import { use2GISBounds } from './use2GISBounds';
import { DEFAULT_PADDING } from '../MapComponent';
import { IMapComponentProps } from '../MapComponent.types';

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
    fitToShowAllGeometry = false,
    dragging = false,
    zoomControl = false,
    zoomControlPosition = 'bottomCenter',
    onClick,
    onCoordinatesClick,
    onCenterChanged,
    boundsPadding = DEFAULT_PADDING,
  } = props;

  const bounds2GIS = use2GISBounds({ markers, polylines });
  const bounds = fitToShowAllGeometry ? bounds2GIS : undefined;
  const center = position ? [position.longitude, position.latitude] : MOSCOW;
  const centerPosition = bounds ? [
    (bounds.northEast[0] + bounds.southWest[0]) * 0.5,
    (bounds.northEast[1] + bounds.southWest[1]) * 0.5,
  ] : center;

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

  const renderSVG = (idx: number, lastIdx: number) => {
    if (idx === lastIdx) {
      return blueEllipse;
    }
    if (idx === 0) {
      return greenEllipse;
    }
    return greenSmallEllipse;
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
          zoomControl={zoomControl ? zoomControlPosition : false}
          copyright="bottomCenter"
          bounds={bounds}
          onClick={onMapClick}
          onCenterChange={onMoveEnd}
          boundsPadding={boundsPadding}
        >
          {markers?.map((marker, index, markers) => (
            <Marker2GIS
              icon={renderSVG(index, markers.length - 1)}
              rotation={undefined}
              key={uuid()}
              coordinates={[marker.longitude as number, marker.latitude as number]}
              userData={marker}
              size={[24, 24]}
              anchor={[12, 12]}
            />
          ))}

          {polylines?.map(segment => (
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
