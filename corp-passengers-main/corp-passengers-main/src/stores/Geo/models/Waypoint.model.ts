import { Waypoint } from '../Geo.interface';

export class WaypointModel implements Waypoint {
  country?: string;

  region?: string;

  city?: string;

  street?: string;

  house?: string;

  building?: string;

  structure?: string;

  district?: string;

  latitude = 0;

  longitude = 0;

  waitTime = 0;

  private _addressString = '';

  get isValid(): boolean {
    return !!this.latitude && !!this.longitude;
  }

  get addressString(): string {
    return this._addressString || this.buildAddressString();
  }

  set addressString(val: string) {
    this._addressString = val ?? '';
  }

  /**
   * @returns время ожидания в минутах
   */
  get waitTimeMinutes(): number {
    return Math.trunc(this.waitTime / 1000 / 60);
  }

  /**
   * @argument val: number - время ожидания в минутах
   */
  set waitTimeMinutes(val: number) {
    this.waitTime = val * 60 * 1000;
  }

  constructor(location?: Waypoint) {
    if (location) {
      this.country = location.country ?? '';
      this.region = location.region ?? '';
      this.city = location.city ?? '';
      this.street = location.street ?? '';
      this.house = location.house ?? '';
      this.building = location.building ?? '';
      this.structure = location.structure ?? '';
      this.district = location.district ?? '';
      this.latitude = location.latitude;
      this.longitude = location.longitude;
      this.waitTime = location.waitTime ?? 0;
    }
    this.addressString = this.buildAddressString();
  }

  buildAddressString(): string {
    return (
      `${this.street ? `${this.street}, ` : ''}`
      + `${this.district ? `${this.district},` : ''}`
      + `${this.house ? `${this.house}, ` : ''}`
      + `${this.city ? `${this.city},` : ''}`
    ).slice(0, -1);
  }
}
