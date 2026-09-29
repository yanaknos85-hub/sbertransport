/* eslint-disable @typescript-eslint/no-explicit-any */
import * as L from 'leaflet';
import * as iconRetinaUrl from 'shared/components/Map/images/marker-icon-2x.png';
import * as iconUrl from 'shared/components/Map/images/marker-icon.png';
import * as shadowUrl from 'leaflet/dist/images/marker-shadow.png';
import 'leaflet/dist/leaflet.css';
import React, { useCallback, useEffect, useState } from 'react';
import {
  FeatureGroup, Map, Marker, TileLayer, ZoomControl, Polyline
} from 'react-leaflet';
import { Coordinates, ISegment, Waypoint } from 'stores/Geo/Geo.interface';
import uuid from 'utils/uuid';
import greenEllipse from 'shared/assets/svg/greenEllipse.svg';

import './map.css';

// @ts-ignore
delete L.Icon.Default.prototype._getIconUrl;

L.Icon.Default.mergeOptions({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
});
export type LatLngTuple = [number, number];

interface IMapComponentProps {
  position?: Coordinates;
  markers?: Waypoint[];
  mapClickHandler?: (point: Coordinates) => void;
  className?: any;
  dragging?: boolean;
  locate?: boolean;
  polylines?: ISegment[];
  zoomControl?: boolean;
  maxZoom?: number;
  doubleClickZoom?: boolean;
  outerMapRef?: any;
  onMapReady?: () => void;
}

function useRefLeaflet<T extends React.Component>(): [T | null, React.LegacyRef<T>] {
  const [inst, setInst] = useState<T | null>(null);
  const ref: React.LegacyRef<T> = useCallback((instance: T | null) => {
    if (instance !== null) {
      setInst(instance);
    }
  }, []);

  return [inst, ref];
}

const MapComponent: React.FC<IMapComponentProps> = props => {
  const {
    position = undefined,
    markers = [],
    className,
    dragging = false,
    zoomControl = false,
    maxZoom = 15,
    doubleClickZoom = false,
    polylines = [],
    outerMapRef,
    onMapReady,
  } = props;

  const [mapInst, mapRef] = useRefLeaflet<Map>();
  const [groupInst, groupRef] = useRefLeaflet<FeatureGroup>();

  const isPositionValid = useCallback(
    (): boolean => Boolean(position?.length && typeof position[0] === 'number' && typeof position[1] === 'number'),
    [position]
  );

  useEffect(() => {
    if (position && isPositionValid()) {
      mapInst?.leafletElement.setView(position, 12);
    }
  }, [position, mapInst, isPositionValid]);

  useEffect(() => {
    const bounds = groupInst?.leafletElement.getBounds();
    if (bounds && Object.keys(bounds).length && markers.length) {
      mapInst?.leafletElement.fitBounds(bounds, { padding: [40, 40] });
    }
  }, [markers, groupInst, mapInst]);

  const insideRouteIcon = new L.Icon({
    iconUrl: greenEllipse,
    iconRetinaUrl: greenEllipse,
    popupAnchor: [-0, -0],
    iconSize: [25, 25],
  });

  return (
    <Map
      center={position}
      zoom={12}
      maxZoom={maxZoom}
      scrollWheelZoom={false}
      attributionControl={false}
      zoomControl={false}
      doubleClickZoom={doubleClickZoom}
      dragging={dragging}
      animate
      easeLinearity={0.35}
      ref={outerMapRef || mapRef}
      className={className}
      whenReady={onMapReady}
    >
      <TileLayer url="http://tile2.maps.2gis.com/tiles?x={x}&y={y}&z={z}" />
      {zoomControl && (
      <ZoomControl
        position="bottomright"
        zoomInText="+"
        zoomOutText="-"
      />
      )}
      <FeatureGroup ref={groupRef}>
        {markers.map((x: Waypoint) => {
          const title = `${x?.city} ${x?.street} ${x?.house}`;
          return x.latitude && x.longitude ? (
            // @ts-ignore
            <Marker
              key={uuid()}
              position={[x.latitude, x.longitude]}
              title={title}
              icon={insideRouteIcon}
            />
          ) : null;
        })}
      </FeatureGroup>
      {!!polylines?.length
      && polylines.map((x: ISegment) => (
        <Polyline key={uuid()} positions={x.coordinates.map(y => [y.latitude, y.longitude] as LatLngTuple)} />
      ))}
    </Map>
  );
};

export default MapComponent;
