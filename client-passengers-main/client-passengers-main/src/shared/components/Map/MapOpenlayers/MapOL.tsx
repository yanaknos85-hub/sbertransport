import React, { Suspense, useEffect, useState } from 'react';
import type { FC } from 'react';
import { DEFAULT_ZOOM, MOSCOW } from '@sber-sbertransport/ui-kit/src';
import { useGeographic } from 'ol/proj';
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

import ErrorBoundary from 'shared/components/ErrorBoundary';
import SpinWrapped from 'shared/components/SpinWrapped';

import markerIcon from '../images/marker-icon.png';
import { IMapComponentProps } from '../MapComponent.types';
import { DEFAULT_PADDING } from '../MapComponent';
import { TileSource2GIS } from './TileSource2GIS/TileSource2GIS';
import Car from 'shared/components/Images/car_taxi.svg';
import Map_point from 'shared/components/Images/map_point.svg';

import styles from './style.module.scss';

const LONGITUDE_250_METERS = 0.009 * 0.25;
const LATITUDE_250_METERS_AT_55_DEG_LONGITUDE = 0.015 * 0.25;

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
    zoomControl = false,
    fitToShowAllGeometry = true,
    // у rlayers нет управления положением дефолтных контролов карты.
    // если бизнес захочет - можно сделать через кастомные контролы
    onClick,
    onCoordinatesClick,
    onCenterChanged,
    boundsPadding = DEFAULT_PADDING,
    carPosition,
    isCarMonitoring,
  } = props;

  useGeographic();

  const [view, setView] = useState<RView>({
    center: position ? [position.longitude, position.latitude] : MOSCOW,
    zoom: DEFAULT_ZOOM,
  });
  const [isFitRequired, setIsFitRequired] = useState(true);
  const route = polylines.map(polyline => polyline.coordinates
    .map(point => [point.longitude as number, point.latitude as number])
  ).flat();

  const onMapClick = (e: MapBrowserEvent<UIEvent>) => {
    if (!e.coordinate) return;
    const [longitude, latitude] = e.coordinate;
    onClick && onClick(e);
    onCoordinatesClick && onCoordinatesClick({ latitude, longitude });
  };

  useEffect(() => {
    setIsFitRequired(!!carPosition || markers.length > 0);
  }, [carPosition, markers]);

  useEffect(
    () => {
      const [longitude, latitude] = view.center;
      onCenterChanged && onCenterChanged({ latitude, longitude });
    },
    [view.center]
  );

  const fitBounds = (e: RenderEvent) => {
    if (fitToShowAllGeometry && isFitRequired) {
      setIsFitRequired(false);
      let extent;

      if (isCarMonitoring && carPosition) {
        extent = boundingExtent([
          [carPosition.longitude, carPosition.latitude],
          [markers[0].longitude, markers[0].latitude],
        ]);
      } else if (isCarMonitoring || markers.length < 2) {
        extent = [
          markers[0].longitude - LONGITUDE_250_METERS,
          markers[0].latitude - LATITUDE_250_METERS_AT_55_DEG_LONGITUDE,
          markers[0].longitude + LONGITUDE_250_METERS,
          markers[0].latitude + LATITUDE_250_METERS_AT_55_DEG_LONGITUDE,
        ];
      } else {
        const coordinates = route.length > 0 ? route
          : markers.map(marker => [marker.longitude, marker.latitude]);
        extent = boundingExtent(coordinates);
      }

      e.target.getView().fit(
        extent,
        {
          duration: 250,
          maxZoom: 18.5,
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
          {zoomControl && <RControl.RZoomSlider />}
          <RControl.RAttribution />

          {!!markers.length && !isCarMonitoring && (
            <RLayerVector
              zIndex={10}
            >
              {markers.map(marker => (
                <RFeature
                  geometry={new Point([marker.longitude, marker.latitude])}
                  key={uuid()}
                >
                  <ROverlay>
                    <img
                      src={markerIcon}
                      style={{
                        position: 'relative',
                        top: -40,
                        left: -12,
                        pointerEvents: 'none',
                      }}
                      width={24}
                      height={48}
                      alt="marker"
                    />
                  </ROverlay>
                </RFeature>
              ))}
            </RLayerVector>
          )}
          {isCarMonitoring && carPosition && (
            <RLayerVector>
              <RFeature geometry={new Point([carPosition.longitude, carPosition.latitude])}>
                <ROverlay>
                  <img
                    src={Car}
                    style={{
                      position: 'relative',
                      top: -40,
                      left: -12,
                      pointerEvents: 'none',
                    }}
                    width={48}
                    height={96}
                    alt="marker"
                  />
                </ROverlay>
              </RFeature>
            </RLayerVector>
          )}
          {!!markers.length && isCarMonitoring && (
            <RLayerVector
              zIndex={10}
            >
              <RFeature
                geometry={new Point([markers[0].longitude, markers[0].latitude])}
                key={uuid()}
              >
                <ROverlay>
                  <img
                    src={Map_point}
                    style={{
                      position: 'relative',
                      top: -40,
                      left: -12,
                      pointerEvents: 'none',
                    }}
                    width={24}
                    height={48}
                    alt="marker"
                  />
                </ROverlay>
              </RFeature>
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
