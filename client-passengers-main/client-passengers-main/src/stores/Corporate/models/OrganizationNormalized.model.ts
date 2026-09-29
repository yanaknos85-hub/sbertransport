import { Dictionary } from 'lodash';
import mapKeys from 'lodash/mapKeys';

import { IOrganization, IShortDepartment, IShortPosition } from '../Corporate.interface';

export class OrganizationNormalizedModel {
  id: string;

  officialName: string;

  address: string;

  positions: Dictionary<IShortPosition>;

  departments: Dictionary<IShortDepartment>;

  constructor(org: IOrganization) {
    this.id = org.id;
    this.officialName = org.officialName;
    this.address = org.address;
    this.positions = mapKeys(org.positions, 'id');
    this.departments = mapKeys(org.departments, 'id');
  }
}
