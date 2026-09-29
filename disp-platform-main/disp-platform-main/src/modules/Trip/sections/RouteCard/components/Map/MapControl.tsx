import {
  FC, JSXElementConstructor, ReactElement, useEffect
} from 'react';
import ReactDOMServer from 'react-dom/server';
import { HTMLMarker2GISProps, use2GIS } from '@sber-sbertransport/ui-kit/src';

type Position = 'topLeft' | 'topCenter' | 'topRight' | 'centerLeft' | 'centerRight' | 'bottomLeft' | 'bottomCenter' | 'bottomRight';

interface ControlOptions {
  position: Position;
}

export const useMapControl = (
  map: HTMLMarker2GISProps['map'],
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  children: ReactElement<any, string | JSXElementConstructor<any>>,
  options: ControlOptions // При переносе в ui-kit заменить на 2gis ControlOptions
) => {
  const { bundle } = use2GIS();

  useEffect(() => {
    if (!map || !bundle) {
      return;
    }

    const control = new bundle.Control(
      map,
      ReactDOMServer.renderToString(children),
      options
    );

    return () => {
      control?.destroy();
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [map, bundle, children, JSON.stringify(options)]);
};

const MapControl: FC<{
  map?: HTMLMarker2GISProps['map'];
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  children: ReactElement<any, string | JSXElementConstructor<any>>;
  options?: ControlOptions;
}> = ({
  map,
  children,
  options = { position: 'topLeft' },
}) => {
  useMapControl(map, children, options);

  return null;
};

export default MapControl;
