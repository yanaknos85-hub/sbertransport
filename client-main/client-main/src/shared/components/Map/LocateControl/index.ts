import Locate from 'leaflet.locatecontrol';
import { useEffect } from 'react';
import { useLeaflet } from 'react-leaflet';

interface Props {
  options: { position: 'bottomright' | 'topleft' | 'topright' | 'bottomleft' };
  startDirectly: boolean;
}

const LocateControl: React.FC<Props> = ({ options, startDirectly }) => {
  const { map } = useLeaflet();

  useEffect(() => {
    const lc = new Locate(options);
    lc.addTo(map);

    if (startDirectly) {
      // request location update and set location
      lc.start();
    }
    // FIXME react-hooks/exhaustive-deps
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [map]);

  return null;
};

export default LocateControl;
