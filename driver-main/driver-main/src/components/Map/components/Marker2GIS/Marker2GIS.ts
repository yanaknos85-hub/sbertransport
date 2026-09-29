/* eslint-disable react-hooks/exhaustive-deps */
import {
  DynamicObjectPointerEvent, Map, Marker, MarkerOptions
} from '@2gis/mapgl/types';
import { memo, useEffect, useState } from 'react';
import { use2GIS } from '../../context/2gis.context';

export interface Marker2GISProps extends MarkerOptions {
  map?: Map;
  onClick?: (ev: DynamicObjectPointerEvent<Marker>) => void;
}

export const Marker2GIS = memo(({
  onClick,
  map,
  coordinates,
  rotation,
  ...options
}: Marker2GISProps): null => {
  const [marker, setMarker] = useState<Marker>();

  const { bundle } = use2GIS();

  useEffect(() => {
    if (!map || !bundle) {
      return;
    }

    const newMarker = new bundle.Marker(map, {
      coordinates,
      ...options,
    });
    setMarker(newMarker);

    return () => newMarker.destroy();
  }, [JSON.stringify(options), map, bundle]);

  useEffect(() => {
    if (!marker || !onClick) {
      return;
    }

    marker.on('click', onClick);

    return () => {
      marker.off('click', onClick);
    };
  }, [marker, JSON.stringify(onClick)]);

  useEffect(() => {
    marker?.setCoordinates(coordinates);
  }, [marker, JSON.stringify(coordinates)]);

  useEffect(() => {
    if (!rotation) return;

    marker?.setRotation(rotation);
  }, [marker, rotation]);

  return null;
});
