import { ReactNode } from 'react';
import { injectable } from 'inversify';
import { action, observable } from 'mobx';
import { UUID } from 'utils/io-ts';
import uuid from 'utils/base/uuid';
import { Segment } from 'api/services/Geo/Geo.types';
import dayjs from 'dayjs';

interface Point {
  id: UUID;
  longitude: number;
  latitude: number;
  icon?: ReactNode;
}

interface Route extends Segment {
  id: UUID;
}

const DEFAULT_CENTER: [number, number] = [0, 0];
const DEFAULT_ZOOM = 18;

@injectable()
export class MapStore {
  private watchId: number | null = null;

  /** [longitude, latitude] */
  @observable center: [number, number] = DEFAULT_CENTER;
  @observable geoCenter: [number, number] | undefined;
  @observable watchingGeolocation = false;
  @observable errorWatchingGeo = false;
  @observable zoom = DEFAULT_ZOOM;
  @observable points: Point[] = [];
  @observable routes: Route[] = [];
  @observable expectedTime: number | undefined;
  @observable expectedDistance: number | undefined;
  @observable expectedArrivalTime: dayjs.Dayjs | null = null;

  /** [longitude, latitude] */
  @action.bound setCenter(center: [number, number]) {
    this.center = center;
  }

  @action.bound setExpectedTime(expectedTime: number) {
    this.expectedTime = expectedTime;
  }

  @action.bound setExpectedDistance(expectedDistance: number) {
    this.expectedDistance = expectedDistance;
  }

  @action.bound setExpectedArrivalTime(expectedArrivalTime: dayjs.Dayjs | null) {
    this.expectedArrivalTime = expectedArrivalTime;
  }

  @action.bound watchGeolocation(fallbackCenter?: [number, number]) {
    if (this.watchingGeolocation) return;

    this.watchingGeolocation = true;

    this.watchId = navigator.geolocation.watchPosition(
      position => {
        this.errorWatchingGeo = false;
        this.geoCenter = [position.coords.longitude, position.coords.latitude];
        this.center = [position.coords.longitude, position.coords.latitude];
      },
      () => {
        // eslint-disable-next-line no-console
        console.info('Unable to get geolocation');

        this.watchingGeolocation = false;
        this.watchId = null;

        this.errorWatchingGeo = true;
        this.geoCenter = undefined;

        if (fallbackCenter) {
          this.center = fallbackCenter;
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 5000,
        maximumAge: 0,
      }
    );
  }

  @action.bound cancelWatchingGeolocation() {
    if (this.watchId) {
      this.errorWatchingGeo = false;
      this.watchingGeolocation = false;
      this.geoCenter = undefined;
      navigator.geolocation.clearWatch(this.watchId);
    }
  }

  @action.bound getCurrentLocation() {
    return {
      coords: this.geoCenter,
      zoom: DEFAULT_ZOOM,
    };
  }

  @action.bound setPoints(points: Omit<Point, 'id'>[]) {
    this.points = [...points.map(point => ({
      ...point,
      id: uuid(),
    }))];
  }

  @action.bound setRoutes(routes: Omit<Route, 'id'>[]) {
    this.routes = [...routes.map(route => ({
      ...route,
      id: uuid(),
    }))];
  }

  @action.bound clear() {
    this.points = [];
    this.routes = [];
  }
}
