/* eslint-disable no-console */
import Locate from 'leaflet.locatecontrol';
import { useEffect } from 'react';
import { useLeaflet } from 'react-leaflet';

interface Props {
  options: any;
  startDirectly: boolean;
}

const LocateControl: React.FC<Props> = ({ options }) => {
  const { map } = useLeaflet();

  useEffect(() => {
    if (map) {
      const lc = new Locate({
        ...options,
        onLocationError: () => {
          // геолокация не работает (без этого каллбека будет стрелять промт!)
          console.log('user geolcation denied!');
        },
      });

      lc.addTo(map);
    }
  }, [map]);

  return null;
};

export default LocateControl;
