import { Contacts, Coordinates, WaypointExchange } from '../types';

export class WaypointExchangeModel implements WaypointExchange {
  id: string;
  type: string;
  address: {
    addressStringRepresentation: string;
    coordinates: Coordinates;
  };

  orderingIndex: number;
  contacts: Contacts[];

  constructor(data: WaypointExchange) {
    this.id = data.id;
    this.type = data.type;
    this.address = data.address;
    this.orderingIndex = data.orderingIndex;
    this.contacts = data.contacts;
  }

  get isValid(): boolean {
    return !!(this.address.coordinates.latitude && this.address.coordinates.longitude);
  }
}
