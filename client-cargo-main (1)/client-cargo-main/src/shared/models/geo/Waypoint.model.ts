import { TWaypoint } from './types';

export interface Contact {
  id: string;
  mobilePhone: string;
  employeeId: string;
  fullName: string;
  firstName?: string;
  lastName?: string;
  organizationId?: string;
  patronymic?: string;
}

export class WaypointModel implements TWaypoint {
  addressStringRepresentation?: string;

  country?: string;

  region?: string;

  city?: string;

  district?: string;

  settlement?: string;

  livingArea?: string;

  place?: string;

  street?: string;

  house?: string;

  building?: string;

  structure?: string;

  latitude = 0;

  longitude = 0;

  waitTime = 0;

  type?: string;

  typePoint?: string;

  organization?: string;

  contacts?: Contact[];

  existInVspGosbTbRegistry = false;

  addressType = 'ORDINARY';

  gosb?: string;

  kic?: any;

  name?: string;

  vsp?: string;

  checkinAutomatic?: boolean;

  checkinManual?: boolean;

  absenceReason?: string;

  icon?: string;

  orderingIndex?: number;

  entrance?: number;

  floor?: number;

  flat?: number;

  private _addressString = '';

  private _regionData = '';

  get isValid(): boolean {
    return !!this.latitude && !!this.longitude;
  }

  get addressString(): string {
    return this.buildAddressString();
  }

  set addressString(val: string) {
    this._addressString = this.createAddress();
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
      this.district = location.district;
      this.city = location.city;
      this.livingArea = location.livingArea;
      this.settlement = location.settlement;
      this.place = location.place;
      this.street = location.street;
      this.house = location.house;
      this.building = location.building;
      this.structure = location.structure;
      this.latitude = location.latitude ?? 0;
      this.longitude = location.longitude ?? 0;
      this.waitTime = location.waitTime ?? 0;
      this.type = location.type;
      this.organization = location.organization;
      this.contacts = location.contacts;
      this.addressStringRepresentation = location.addressStringRepresentation ? location.addressStringRepresentation : undefined;
      this.checkinAutomatic = location.checkinAutomatic;
      this.checkinManual = location.checkinManual;
      this.absenceReason = location.absenceReason;
      this.gosb = location.gosb;
      this.name = location.name;
      this.vsp = location.vsp;
      this.addressType = location.addressType ?? 'ORDINARY';
      this.kic = location.kic;
      this.orderingIndex = location.orderingIndex;
      this.typePoint = location.typePoint;
    }

    this.addressString = this.buildAddressString();
    this.existInVspGosbTbRegistry = this.getExistInVspGosbTbRegistry();
  }

  public getFormattedAddressWithDetails(): string {
    const baseAddress = this.buildAddressString();
    const additionalParts: string[] = [];

    if (this.entrance) additionalParts.push(`подъезд ${this.entrance}`);
    if (this.floor) additionalParts.push(`этаж ${this.floor}`);
    if (this.flat) additionalParts.push(`кв/офис ${this.flat}`);

    if (additionalParts.length > 0) {
      return `${baseAddress}, ${additionalParts.join(', ')}`;
    }
    return baseAddress;
  }

  getExistInVspGosbTbRegistry(): boolean {
    if (this.addressType === 'VSP') {
      return true;
    }
    return this.addressType === 'VSP_KIC';
  }

  buildAddressString(): string {
    if (this.addressType !== 'ORDINARY') {
      return (
        `${this.createAddress()} , ${this.name ? `${this.name}` : `${this.vsp}`}`
      );
    }
    return this.createAddress();
  }

  private createAddress(): string {
    return (
      `${this.region ? this.region === this.city ? '' : `${this.region}, ` : ''}`
      + `${this.district ? `${this.district}, ` : ''}`
      + `${this.city ? `${this.city.replace(/\s/g, ' ')}, ` : ''}`
      + `${this.livingArea ? `${this.livingArea}, ` : ''}`
      + `${this.settlement ? `${this.settlement}, ` : ''}`
      + `${this.place ? `${this.place}, ` : ''}`
      + `${this.street ? `${this.street}, ` : ''}`
      + `${this.house ? `${this.house}` : ''}`
      + `${this.building ? `${this.building}, ` : ''}`
      + `${this.structure ? `${this.structure}, ` : ''}`
    ).replace(/,\s*$/, '').trim();
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
