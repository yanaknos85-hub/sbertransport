import Taxi from 'shared/images/transport/Taxi.png';
import Courier from 'shared/images/transport/Courier.png';
import Repair from 'shared/images/transport/Repair.png';
import Parking from 'shared/images/transport/Parking.png';

import { Tab } from '../../constants/tabs.constants';

const iconUrl = {
  [Tab.passengers]: Taxi,
  [Tab.cargo]: Courier,
  [Tab.carService]: Repair,
  [Tab.parking]: Parking,
};

export default iconUrl;
