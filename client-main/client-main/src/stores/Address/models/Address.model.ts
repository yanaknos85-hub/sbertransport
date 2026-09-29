import { TWaypoint } from '@sber-sbertransport/mf-core';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { TAddressExist } from 'stores/Address/Address.interface';

export class AddressModel extends WaypointModel implements TAddressExist {
  id: string;

  label?: string;

  address?: TWaypoint;

  constructor(address: TAddressExist) {
    super(address);
    this.label = address.label;
    this.id = address.id;
    this.address = address.address;
  }
}
