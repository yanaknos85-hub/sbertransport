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

import markerIcon from '../images/marker-icon.png';
import { IMapComponentProps } from '../MapComponent.types';
import { DEFAULT_PADDING } from '../MapComponent';
import { TileSource2GIS } from './TileSource2GIS/TileSource2GIS';

import styles from './style.module.scss';

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
    fitToShowAllGeometry = false,
    // у rlayers нет управления положением дефолтных контролов карты.
    // если бизнес захочет - можно сделать через кастомные контролы
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
          {zoomControl && <RControl.RZoomSlider />}
          <RControl.RAttribution />

          {!!markers.length && (
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
