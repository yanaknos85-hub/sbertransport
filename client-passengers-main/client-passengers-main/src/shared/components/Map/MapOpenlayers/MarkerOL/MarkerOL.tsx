import React, { FC } from 'react';
import {
  MapBrowserEvent, RContextType, RFeature, RFeatureProps, ROverlay
} from 'rlayers';
import { Point } from 'ol/geom';
import { fromLonLat } from 'ol/proj';
import { Coordinates } from '../../MapComponent.types';
import defaultMarkerIcon from '../../images/marker-icon.png';

// поменять при смене дефолтной иконки
const DEFAULT_ICON_SIZE_PX = [24, 48];
const DEFAULT_ANCHOR_PX = [12, 40];

export interface MarkerProps {
  /** координаты */
  coordinates: Coordinates;
  /** URI иконки */
  icon?: string;
  /** вращение иконки */
  rotation?: number;
  /** размер иконки, пкс */
  size?: number[];
  /** смещение иконки, пкс - если нужно, чтобы в точку указывал не верхний левый угол иконки */
  anchor?: number[];
  userData?: any;
  onClick?: (event: MapBrowserEvent<UIEvent>) => void;
  onCoordinatesClick?: (coordinates: Coordinates) => void;
}

/** Маркер-иконка rlayers */
export const MarkerOL: FC<MarkerProps & RFeatureProps & React.Context<RContextType>> = ({
  coordinates,
  icon = defaultMarkerIcon,
  rotation = 0,
  size = DEFAULT_ICON_SIZE_PX,
  anchor = DEFAULT_ANCHOR_PX,
  onClick,
  onCoordinatesClick,
  ...props
}) => {
  const onMarkerClick = (e: MapBrowserEvent<UIEvent>) => {
    if (!e.coordinate) return;
    const [longitude, latitude] = e.coordinate;
    onClick && onClick(e);
    onCoordinatesClick && onCoordinatesClick({ latitude, longitude });
  };

  return (
    <RFeature
      geometry={new Point(fromLonLat([coordinates.latitude, coordinates.longitude]))}
      onClick={onMarkerClick}
      {...props}
    >
      <ROverlay>
        <img
          src={icon}
          style={{
            position: 'relative',
            top: -anchor[1],
            left: -anchor[0],
            pointerEvents: 'none',
            transform: `rotate(${rotation}deg)`,
          }}
          width={size[0]}
          height={size[1]}
          alt="marker"
        />
      </ROverlay>
    </RFeature>
  );
};
