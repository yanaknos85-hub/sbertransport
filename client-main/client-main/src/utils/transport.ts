import {
  FleetManagementTypes,
  TransportTypesCargo,
  TransportTypesPassenger
} from 'stores/TransportTypes/TransportTypes.interface';

import carshImg from 'shared/components/Images/orders/carsh.png';
import persImg from 'shared/components/Images/orders/pers.png';
import personalImg from 'shared/components/Images/orders/personal.png';
import publicImg from 'shared/components/Images/orders/public.png';
import regCargoImg from 'shared/components/Images/orders/regCargo.png';
import repairImg from 'shared/components/Images/orders/repair.png';
import taxiImg from 'shared/components/Images/orders/taxi.png';
import cargo from 'shared/components/Images/orders/cargo.png';
import courier from 'shared/components/Images/orders/courier.png';
import collector from 'shared/components/Images/orders/collector.png';

const transportIconTable = {
  [TransportTypesPassenger.PUBLIC]: publicImg,
  [TransportTypesPassenger.PERSONAL]: persImg,
  [TransportTypesPassenger.TAXI]: taxiImg,
  [TransportTypesPassenger.CARSHARING]: carshImg,
  [TransportTypesCargo.COURIER]: courier,
  [TransportTypesCargo.DEDICATED]: cargo,
  [TransportTypesCargo.INTERREGIONAL]: regCargoImg,
  [TransportTypesCargo.DOMESTIC_COURIER]: courier,
  [TransportTypesCargo.INDIVIDUAL]: personalImg,
  [FleetManagementTypes.OFFICIAL]: repairImg,
  [FleetManagementTypes.PRIVATE]: personalImg,
  [FleetManagementTypes.SPECIAL]: collector,
};

export const getTransportIcon = (transportType: string) => {
  if (transportType) {
    return transportIconTable[transportType];
  }

  return personalImg;
};
