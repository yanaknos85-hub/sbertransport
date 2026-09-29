/* eslint-disable  @typescript-eslint/no-explicit-any */
import { useEffect } from 'react';
import {
  HTMLMarker2GIS,
  Map2GIS, Map2GISProvider, Marker2GIS, MOSCOW,
  Polyline2GIS
} from 'components/Map';
import { API_KEY_2GIS } from 'constants/env.constants';
import Geo from 'assets/icons/geo.svg';
import { observer } from 'mobx-react';
import { useAppStore } from 'stores/stores.context';
import WaypointIcon from 'components/WaypointIcon/WaypointIcon';
import { CheckinType } from 'constants/trips.constants';

interface Props {
  handleClick: React.Dispatch<React.SetStateAction<any>>;
}

const Map = observer(({ handleClick }: Props) => {
  const { mapStore } = useAppStore();

  useEffect(() => {
    mapStore.watchGeolocation(MOSCOW);

    navigator.permissions.query({ name: 'geolocation' }).then(status => {
      status.onchange = () => {
        mapStore.watchGeolocation(MOSCOW);
      };
    });

    return () => {
      mapStore.cancelWatchingGeolocation();
    };
  }, [mapStore]);

  return (
    <Map2GISProvider APIKey={API_KEY_2GIS}>
      <Map2GIS
        containerStyle={{ height: '100%' }}
        zoomControl="centerRight"
        center={mapStore.center}
        zoom={mapStore.zoom}
        onGeolocation={mapStore.getCurrentLocation}
        onClick={event => handleClick(event.lngLat)}
      >
        {!!mapStore.geoCenter && (
          <Marker2GIS
            icon={Geo}
            coordinates={mapStore.geoCenter}
          />
        )}

        {mapStore.points.map((waypoint, index) => (
          <HTMLMarker2GIS
            key={waypoint.id}
            coordinates={[waypoint.longitude, waypoint.latitude]}
          >
            <WaypointIcon index={index} checkinType={CheckinType.AUTO} />
          </HTMLMarker2GIS>
        ))}

        {mapStore.routes.map(route => (
          <Polyline2GIS
            key={route.id}
            coordinates={route.coordinates.map(point => [point.longitude, point.latitude])}
            color="#0078D9"
            width={6}
          />
        ))}
      </Map2GIS>
    </Map2GISProvider>
  );
});

export default Map;
