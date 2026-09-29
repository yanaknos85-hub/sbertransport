import React, { FC, Suspense, useEffect } from 'react';
import { DEFAULT_ZOOM, MOSCOW } from '@sber-sbertransport/ui-kit/src';
import { useGeographic } from 'ol/proj';
import { Coordinate } from 'ol/coordinate';
import { LineString, Point } from 'ol/geom';
import { boundingExtent } from 'ol/extent';
import 'ol/ol.css';
import {
  MapBrowserEvent,
  RControl,
  RenderEvent,
  RFeature,
  RLayerVector,
  RMap,
  ROverlay,
  RStyle
} from 'rlayers';
import { RView } from 'rlayers/RMap';

import uuid from 'utils/uuid';
import { LONGITUDE_250_METERS, LATITUDE_250_METERS_AT_55_DEG_LONGITUDE } from 'constants/constants.geo';
import ErrorBoundary from 'shared/components/ErrorBoundary';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';
import greenEllipse from 'shared/assets/svg/greenEllipse.svg';
import grayEllipse from 'shared/assets/svg/grayEllipse.svg';
import blueEllipse from 'shared/assets/svg/blueEllipse.svg';

import { IMapComponentProps } from '../MapComponent.types';
import { DEFAULT_PADDING } from '../MapComponent';
import { TileSource2GIS } from './TileSource2GIS/TileSource2GIS';
import { MarkerWithLetter } from './MarkerWithLetter';

import styles from './style.module.scss';

export interface Viewport {
  center: [number, number] | null | undefined;
  zoom: number | null | undefined;
}
export type CenterCoordinates = Viewport['center'];

const getLetterByIndex = (index: number): string => {
  return String.fromCharCode(65 + index);
};

export const MapComponent: FC<IMapComponentProps> = props => {
  const {
    position = undefined,
    markers = [],
    polylines = [],
    zoomControl = false,
    fitToShowAllGeometry = false,
    showMarkerAddress = false,
    zoomControlPosition = 'topLeft',
    onClick,
    onCoordinatesClick,
    onCenterChanged,
    boundsPadding = DEFAULT_PADDING,
  } = props;

  useGeographic();
  const center = position ? [position.longitude, position.latitude] : MOSCOW;
  const [view, setView] = React.useState<RView>({ center: center as Coordinate, zoom: DEFAULT_ZOOM });
  const [isFitRequired, setIsFitRequired] = React.useState(true);

  const route = polylines.map(polyline => polyline.coordinates.map(point => [point.longitude, point.latitude])).flat();

  const onMapClick = (e: MapBrowserEvent<UIEvent>) => {
    if (!e.coordinate) return;
    const [longitude, latitude] = e.coordinate;
    onClick && onClick(e);
    onCoordinatesClick && onCoordinatesClick({ latitude, longitude });
  };

  useEffect(
    () => {
      setIsFitRequired(markers.length > 0);
    },
    [markers]
  );

  useEffect(
    () => {
      const [longitude, latitude] = view.center;
      onCenterChanged && onCenterChanged({ latitude, longitude });
    },
    [view.center, onCenterChanged]
  );

  const renderSVG = (idx: number, lastIdx: number) => {
    if (idx === 0) {
      return greenEllipse;
    }
    if (idx === lastIdx) {
      return blueEllipse;
    }
    return grayEllipse;
  };

  const fitBounds = (e: RenderEvent) => {
    if (fitToShowAllGeometry && isFitRequired) {
      setIsFitRequired(false);
      const markerPoints = markers.map(marker => [marker.longitude, marker.latitude]);
      const extent = markers.length > 1
        ? boundingExtent(route.length > 0 ? route : markerPoints)
        : [
          markers[0].longitude - LONGITUDE_250_METERS,
          markers[0].latitude - LATITUDE_250_METERS_AT_55_DEG_LONGITUDE,
          markers[0].longitude + LONGITUDE_250_METERS,
          markers[0].latitude + LATITUDE_250_METERS_AT_55_DEG_LONGITUDE,
        ];
      e.target.getView().fit(
        extent,
        {
          duration: 250,
          padding: [
            boundsPadding.top,
            boundsPadding.right,
            boundsPadding.bottom,
            boundsPadding.left,
          ],
        });
    }
  };

  return (
    <ErrorBoundary>
      <Suspense fallback={<SpinWrapped />}>
        <RMap
          className={styles.mapWrapper}
          noDefaultControls={true}
          initial={view}
          view={[view, setView]}
          onClick={onMapClick}
          onRenderComplete={fitBounds}
        >
          <TileSource2GIS />
          {zoomControl && <RControl.RScaleLine />}
          {zoomControl && <RControl.RZoom className={styles[zoomControlPosition]} key={zoomControlPosition} />}

          <RControl.RAttribution />

          {!!markers.length && (
            <RLayerVector
              zIndex={10}
            >
              {markers
                .filter(marker => marker.longitude && marker.latitude)
                .map((marker, index, markers) => {
                  const letter = getLetterByIndex(index);

                  return (
                    <RFeature
                      geometry={new Point([marker.longitude, marker.latitude])}
                      key={uuid()}
                    >
                      <ROverlay>
                        <MarkerWithLetter
                          src={renderSVG(index, markers.length - 1)}
                          letter={letter}
                          alt="marker"
                          style={{
                            position: 'relative',
                            top: -12,
                            left: -12,
                            pointerEvents: 'none',
                          }}
                        />
                        {showMarkerAddress && `№${index + 1} Адрес: ${marker?.city} ${marker?.street} ${marker?.house}`}
                        {/* тут непонятно. видимо это какие-то рудименты,
                        так как везде где я увидел, в карту передаются вейпойнты только с коорд без адреса.
                        пока скрыто этим пропсом. и в 2гис компонент это пока не стал добавлять */}
                      </ROverlay>
                    </RFeature>
                  );
                })}
            </RLayerVector>
          )}

          {!!route.length && (
            <RLayerVector zIndex={8}>
              <RFeature
                geometry={
                  new LineString(route)
                }
                key={uuid()}
              >
                <RStyle.RStyle>
                  <RStyle.RStroke color="blue" width={2} />
                </RStyle.RStyle>
              </RFeature>
            </RLayerVector>
          )}

        </RMap>
      </Suspense>
    </ErrorBoundary>
  );
};
