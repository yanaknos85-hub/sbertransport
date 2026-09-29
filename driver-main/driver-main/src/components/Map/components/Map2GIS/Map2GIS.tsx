/* eslint-disable react-hooks/exhaustive-deps */
import {
  Children, FC, HTMLProps, ReactElement, cloneElement, memo, useEffect, useRef
} from 'react';
import {
  Map, MapOptions, LngLatBounds, MapPointerEvent, Padding, MapEvent
} from '@2gis/mapgl/types';
import {
  useCenter, useFitBounds, useFullScreen, useMap, useGeoLocation, useZoom
} from './hooks';
import { DEFAULT_CENTER, DEFAULT_ZOOM } from '../../constants/2gis.constants';

export interface Map2GISProps extends Omit<MapOptions, 'key'> {
  containerStyle?: HTMLProps<HTMLDivElement>['style'];
  children?: HTMLProps<HTMLDivElement>['children'];
  onMapCreate?: (map?: Map) => void;
  fullScreenControl?: boolean;
  bounds?: LngLatBounds;
  boundsPadding?: Padding;
  onClick?: (ev: MapPointerEvent) => void;
  onCenterChange?: (coordinates: number[]) => void; // координаты [longitude, latitude]
  onGeolocation: () => { coords: [number, number] | undefined; zoom: number | undefined } | void;
}

const MapContainer: FC<Map2GISProps> = ({
  containerStyle,
  onMapCreate,
  children,
  center = DEFAULT_CENTER,
  zoom = DEFAULT_ZOOM,
  fullScreenControl = false,
  bounds = undefined,
  boundsPadding = undefined,
  onClick,
  onCenterChange,
  onGeolocation,
  ...props
}) => {
  const ref = useRef<HTMLDivElement | null>(null);

  const map = useMap(ref, props);

  useEffect(() => {
    if (!map || !onClick) {
      return;
    }

    map.on('click', onClick);

    return () => {
      map.off('click', onClick);
    };
  }, [map, JSON.stringify(onClick)]);

  useEffect(() => {
    if (!map || !onCenterChange) {
      return;
    }

    const handleCenterChange = (event: MapEvent) => {
      if (event.isUser) {
        onCenterChange(map.getCenter());
      }
    };

    map.on('centerend', handleCenterChange);

    return () => {
      map.off('centerend', handleCenterChange);
    };
  }, [map, JSON.stringify(onCenterChange)]);

  useEffect(() => {
    onMapCreate?.(map);
  }, [map]);

  // Управление картой через пропсы (центр, зум и т.д.)
  useFullScreen(map, ref, fullScreenControl);
  useCenter(map, center);
  useGeoLocation(map, onGeolocation);
  useZoom(map, zoom);
  useFitBounds(map, bounds, boundsPadding);

  return (
    <div
      style={{
        width: '100%', height: '350px', ...containerStyle,
      }}
      ref={ref}
    >
      {Children.map(children, child => !!child && cloneElement(child as ReactElement, { map }))}
    </div>
  );
};

export const Map2GIS = memo(MapContainer);
