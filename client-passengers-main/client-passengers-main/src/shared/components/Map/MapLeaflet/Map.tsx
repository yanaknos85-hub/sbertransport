import L from 'leaflet';
import React, {
  useCallback, useEffect, useRef, useState
} from 'react';
import {
  FeatureGroup, Map, Polyline, TileLayer, Viewport, ZoomControl
} from 'react-leaflet';

import { MAP_TILES } from 'constants/constants.env';

import * as iconUrl from 'shared/components/Map/images/marker-icon.png';
import { useOnScreen } from 'shared/hooks/useOnScreen';
import { LatLngTuple, Segment } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import uuid from 'utils/uuid';

import { CustomMarker } from './CustomMarker';
import LocateControl from './LocateControl';

import 'leaflet/dist/leaflet.css';
import './map.css';

export type CenterCoordinates = Viewport['center'];

export interface IMapComponentProps {
  position?: LatLngTuple;
  markers?: WaypointModel[];
  polylines?: Segment[];
  className?: any;
  dragging?: boolean;
  zoomControl?: boolean;
  doubleClickZoom?: boolean;
  zoomControlPosition?: 'bottomright' | 'topleft' | 'topright' | 'bottomleft' | undefined;
  locateControlPosition?: 'bottomright' | 'topleft' | 'topright' | 'bottomleft' | undefined;
  onCenterChanged?: (center: CenterCoordinates) => void;
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
    polylines = [],
    className,
    dragging = false,
    zoomControl = false,
    doubleClickZoom = false,
    zoomControlPosition = 'bottomright',
    locateControlPosition = 'bottomright',
    onCenterChanged,
  } = props;

  const mapInnerRef = useRef() as React.MutableRefObject<HTMLInputElement>;
  const mapInnerIsVisible = useOnScreen(mapInnerRef);
  const [mapReady, setMapReady] = useState(false);
  const [mapInst, mapRef] = useRefLeaflet<Map>();
  const [groupInst, groupRef] = useRefLeaflet<FeatureGroup>();
  const prevCenterRef = useRef<CenterCoordinates>();

  const isPositionValid = useCallback(
    (): boolean => Boolean(position?.length && typeof position[0] === 'number' && typeof position[1] === 'number'),
    [position]
  );

  const isMarkerValid = (m: LatLngTuple): boolean => Boolean(m[0] !== 0 && m[1] !== 0);

  useEffect(() => {
    if (position && isPositionValid() && mapInst) {
      mapInst.leafletElement.setView(position, 16);
    }
  }, [position, mapInst, isPositionValid]);

  useEffect(() => {
    const bounds = groupInst?.leafletElement.getBounds();
    if (bounds && Object.keys(bounds).length && markers.length && mapInst) {
      mapInst.leafletElement.fitBounds(bounds, { padding: [40, 40] });
    }
  }, [markers, groupInst, mapInst]);

  useEffect(() => {
    if (mapInnerIsVisible && !mapReady && mapInst) {
      setMapReady(true);
      mapInst.leafletElement.invalidateSize();
    }
  }, [mapInst, mapInnerIsVisible, mapReady]);

  const onViewportChanged = useCallback(
    (viewport: Viewport) => {
      const newCenter = viewport.center;
      const prevCenter = prevCenterRef.current;
      if (onCenterChanged) {
        if (newCenter && (!prevCenter || prevCenter[0] !== newCenter[0] || prevCenter[1] !== newCenter[1])) {
          onCenterChanged([...newCenter]);
        } else if (!newCenter && prevCenter !== newCenter) {
          onCenterChanged(newCenter);
        }
      }
      prevCenterRef.current = newCenter;
    },
    [onCenterChanged]
  );

  return (
    <Map
      center={position}
      zoom={12}
      maxZoom={18}
      scrollWheelZoom
      attributionControl={false}
      zoomControl={false}
      doubleClickZoom={doubleClickZoom}
      dragging={dragging}
      animate={true}
      easeLinearity={0.35}
      ref={mapRef}
      className={className}
      onViewportChanged={onViewportChanged}
    >
      <div ref={mapInnerRef} />
      <TileLayer url={MAP_TILES} />
      {zoomControl && (
      <ZoomControl
        position={zoomControlPosition}
        zoomInText="+"
        zoomOutText="-"
      />
      )}
      {mapReady && (
        <LocateControl options={{ position: locateControlPosition }} startDirectly={markers.some(x => !x.isValid)} />
      )}
      <FeatureGroup ref={groupRef}>
        {markers.map(x => {
          const icon = new L.Icon({
            iconUrl: iconUrl.default,
            iconSize: [24, 48],
            iconAnchor: [12, 40],
          });

          return (
            isMarkerValid([x.latitude, x.longitude]) && (
              <CustomMarker
                key={uuid()}
                position={[x.latitude, x.longitude]}
                icon={icon}
              />
            )
          );
        })}
      </FeatureGroup>
      {!!polylines?.length
      && polylines.map((x: Segment) => (
        <Polyline key={uuid()} positions={x.coordinates.map(y => [y.latitude, y.longitude] as LatLngTuple)} />
      ))}
      {props?.children}
    </Map>
  );
};

export default MapComponent;
