import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { TAddressNew } from '../Address.interface';

export class AddressNewModel extends WaypointModel implements TAddressNew {
  label = '';

  constructor(address?: TAddressNew) {
    super(address);
    this.label = address?.label ?? '';
  }
}
