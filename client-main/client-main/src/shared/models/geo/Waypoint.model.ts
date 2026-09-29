import { TWaypoint } from './types';

export class WaypointModel implements TWaypoint {
  country?: string;

  region?: string;

  city?: string;

  street?: string;

  house?: string;

  building?: string;

  structure?: string;

  latitude = 0;

  longitude = 0;

  waitTime = 0;

  checkinAutomatic?: boolean;

  checkinManual?: boolean;

  absenceReason?: string;

  icon?: string;

  private _addressString = '';
  private _addressStringWithRegion = '';

  private _regionData = '';

  get isValid(): boolean {
    return !!this.latitude && !!this.longitude;
  }

  get addressString(): string {
    return this._addressString || this.buildAddressString();
  }

  set addressString(val: string) {
    this._addressString = val ?? '';
  }

  get addressStringWithRegion(): string {
    return this._addressStringWithRegion || this.buildAddressStringWithRegion();
  }

  set addressStringWithRegion(val: string) {
    this._addressStringWithRegion = val ?? '';
  }

  /**
   * @returns время ожидания в минутах
   */
  get waitTimeMinutes(): number {
    return Math.trunc(this.waitTime / 1000 / 60);
  }

  /**
   * @param val: number - время ожидания в минутах
   */
  set waitTimeMinutes(val: number) {
    this.waitTime = val * 60 * 1000;
  }

  get regionData(): string | { country?: string; region?: string; city?: string; street?: string; house?: string } {
    return this._regionData || this.addressStructure();
  }

  constructor(location?: TWaypoint) {
    if (location) {
      this.country = location.country;
      this.region = location.region;
      this.city = location.city;
      this.street = location.street;
      this.house = location.house;
      this.building = location.building;
      this.structure = location.structure;
      this.latitude = location.latitude ?? 0;
      this.longitude = location.longitude ?? 0;
      this.waitTime = location.waitTime ?? 0;
      this.checkinAutomatic = location.checkinAutomatic;
      this.checkinManual = location.checkinManual;
      this.absenceReason = location.absenceReason;
    }

    this.addressString = this.buildAddressString();
    this.addressStringWithRegion = this.buildAddressStringWithRegion();
  }

  buildAddressString(): string {
    return (
      `${this.street ? `${this.street}, ` : ''}`
      + `${this.house ? `${this.house}, ` : ''}`
      + `${this.city ? `${this.city},` : ''}`
    ).slice(0, -1);
  }

  buildAddressStringWithRegion(): string {
    return (
      `${this.region ? `${this.region}, ` : ''}`
      + `${this.city && this.city !== this.region ? `${this.city},` : ''}`
      + `${this.street ? `${this.street}, ` : ''}`
      + `${this.house ? `${this.house}` : ''}`
    ).slice(0, -1);
  }

  addressStructure(): {
    country?: string;
    region?: string;
    city?: string;
    street?: string;
    house?: string;
  } {
    return {
      country: this.country ?? '',
      region: this.region ?? '',
      city: this.city ?? '',
      street: this.street ?? '',
      house: this.house ?? '',
    };
  }
}
