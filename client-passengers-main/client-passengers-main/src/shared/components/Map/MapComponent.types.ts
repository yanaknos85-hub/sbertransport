import { MapPointerEvent } from '@sber-sbertransport/ui-kit/src';
import { MapBrowserEvent } from 'rlayers';
import { Segment } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

/** координаты { latitude: number, longitude: number } */
export interface Coordinates {
  latitude: number;
  longitude: number;
}

/** координаты центра карты, [latitude, longitude] */
export type CenterCoordinates = [number, number] | null | undefined;

/** состояние карты - координаты центра отображаемой области, текущий масштаб */
export interface Viewport {
  /** координаты центра карты, [latitude, longitude] */
  center: CenterCoordinates;

  /** масштаб. 0 - вся карта, 28 - максимальное приближение */
  zoom: number | null | undefined;
}

/** отступы в пикселах от краев контейнера, чтобы маркеры/линии целиком помещались на карте */
export interface Padding {
  top: number;
  right: number;
  bottom: number;
  left: number;
}

/** пропсы для карты унифицированные.
 * в конкретном компоненте карты (2гис/openlayers/leaflet) преобразуются к нужным форматам
 */
export interface IMapComponentProps {
  /**
   * координаты центра карты при первой отрисовке или для управления извне.
   * { latitude: number, longitude: number }
  */
  position?: Coordinates;

  /** точки для маркеров, WaypointModel[] */
  markers?: WaypointModel[];

  /** маршрут, calculatedRoute.segments[] */
  polylines?: Segment[];

  /** разрешено ли пользователю двигать карту */
  dragging?: boolean;

  className?: string;

  /** показывать ли кнопки масштаба */
  zoomControl?: boolean;

  /** положение кнопок масштаба */
  zoomControlPosition?: 'bottomright' | 'topleft' | 'topright' | 'bottomleft' | undefined;

  onCenterChanged?: (coordinates: Coordinates) => void;

  /** изменять ли масштаб, чтобы отобразить все точки/линии (для маршрута), по умолчанию false */
  fitToShowAllGeometry?: boolean;

  /** onClick с ивентом карты, тип ивента зависит от типа карты 2гис/openlayers  */
  onClick?: (event: MapPointerEvent | MapBrowserEvent<UIEvent>) => void;

  /** onClick , в который передаются только координаты, для любого типа карты работает одинаково */
  onCoordinatesClick?: (coordinates: Coordinates) => void;

  /** отступы в пикселах от краев контейнера карты, чтобы маркеры/линии
   *  целиком помещались на карте с учетом этих отступов */
  boundsPadding?: Padding;

  /** координаты автомобиля */
  carPosition?: Coordinates;

  /** указывает ли на автомобиль */
  isCarMonitoring?: boolean;
}
