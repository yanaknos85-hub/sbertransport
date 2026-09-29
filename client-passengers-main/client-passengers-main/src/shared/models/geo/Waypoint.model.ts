import { TWaypoint } from './types';

export class WaypointModel implements TWaypoint {
  country?: string;

  region?: string;

  city?: string;

  street?: string;

  house?: string;

  building?: string;

  structure?: string;

  place?: string;

  livingArea?: string;

  settlement?: string;

  latitude = 0;

  longitude = 0;

  waitTime = 0;

  checkinAutomatic?: boolean;

  checkinManual?: boolean;

  absenceReason?: string;

  icon?: string;

  district?: string;

  active?: boolean;

  name?: string;

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
   * @argument val: number - время ожидания в минутах
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
      this.place = location.place;
      this.livingArea = location.livingArea;
      this.settlement = location.settlement;
      this.latitude = location.latitude ?? 0;
      this.longitude = location.longitude ?? 0;
      this.waitTime = location.waitTime ?? 0;
      this.checkinAutomatic = location.checkinAutomatic;
      this.checkinManual = location.checkinManual;
      this.absenceReason = location.absenceReason;
      this.district = location.district;
      this.active = location.active;
      this.name = location.name;
    }

    this.addressString = this.buildAddressString();
    this.addressStringWithRegion = this.buildAddressStringWithRegion();
  }

  buildAddressString(): string {
    return [
      this.name,
      this.street,
      this.house,
      this.place,
      this.livingArea,
      this.settlement,
      this.city && this.city !== this.region ? this.city : '',
      this.district,
      this.region,
      this.country,
    ].filter(Boolean).join(', ');
  }

  buildAddressStringWithRegion(): string {
    return (
      `${this.region ? `${this.region}, ` : ''}`
      + `${this.city && this.city !== this.region ? `${this.city},` : ''}`
      + `${this.street ? `${this.street}, ` : ''}`
      + `${this.house ? `${this.house}` : ''}`
    );
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
