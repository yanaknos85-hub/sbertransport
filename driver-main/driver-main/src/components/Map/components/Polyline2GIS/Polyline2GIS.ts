import {
  DynamicObjectPointerEvent, Map, Polyline, PolylineOptions
} from '@2gis/mapgl/types';
import { memo, useEffect, useState } from 'react';
import { use2GIS } from '../../context/2gis.context';

export interface Polyline2GISProps extends PolylineOptions {
  map?: Map;
  onClick?: (ev: DynamicObjectPointerEvent<Polyline>) => void;
}

export const Polyline2GIS = memo(({
  onClick,
  map,
  coordinates,
  ...options
}: Polyline2GISProps): null => {
  const [polyline, setPolyline] = useState<Polyline>();

  const { bundle } = use2GIS();

  useEffect(() => {
    if (!map || !bundle) {
      return;
    }

    const newPolyline = new bundle.Polyline(map, {
      coordinates,
      ...options,
    });
    setPolyline(newPolyline);

    return () => newPolyline.destroy();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(options), map, bundle]);

  useEffect(() => {
    if (!polyline || !onClick) {
      return;
    }

    polyline.on('click', onClick);

    return () => {
      polyline.off('click', onClick);
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [polyline, JSON.stringify(onClick)]);

  return null;
});
