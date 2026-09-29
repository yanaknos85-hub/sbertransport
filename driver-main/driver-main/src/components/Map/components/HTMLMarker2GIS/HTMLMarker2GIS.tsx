/* eslint-disable react-hooks/exhaustive-deps */
import { HtmlMarker, HtmlMarkerOptions, Map } from '@2gis/mapgl/types';
import {
  FC, JSXElementConstructor, ReactElement, memo, useEffect, useState
} from 'react';
import { use2GIS } from '../../context/2gis.context';
import ReactDOMServer from 'react-dom/server';

export interface HTMLMarker2GISProps extends Omit<HtmlMarkerOptions, 'html'> {
  map?: Map;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  children: ReactElement<any, string | JSXElementConstructor<any>>;
}

export const HTMLMarker2GIS: FC<HTMLMarker2GISProps> = memo(({
  map,
  children,
  coordinates,
  ...options
}) => {
  const [marker, setMarker] = useState<HtmlMarker>();

  const { bundle } = use2GIS();

  useEffect(() => {
    if (!map || !bundle) {
      return;
    }

    const newMarker = new bundle.HtmlMarker(map, {
      html: ReactDOMServer.renderToString(children),
      coordinates,
      ...options,
    });
    setMarker(newMarker);

    return () => newMarker.destroy();
  }, [JSON.stringify(options), map, children, bundle]);

  useEffect(() => {
    marker?.setCoordinates(coordinates);
  }, [marker, JSON.stringify(coordinates)]);

  return null;
});
