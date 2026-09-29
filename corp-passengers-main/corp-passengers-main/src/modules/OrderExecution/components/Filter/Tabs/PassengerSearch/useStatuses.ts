import { useMemo } from 'react';
import { TRANSPORT_TYPE } from 'stores/Limits/Models/ResharedDepartmentLimits';
import { TransportTypeDescriptions } from 'stores/TransportTypes/TransportTypes.interface';

import { IStatus } from 'modules/OrderExecution/interfaces/Orders.interface';
import {
  TAXI_STATUSES,
  CARSHARING_STATUSES,
  PUBLIC_STATUSES,
  PERSONAL_STATUSES,
  GROUP_TRANSFER_STATUSES,
  STATUSES
} from 'modules/OrderExecution/constants/Statuses';

/**
 *
 * @param transportType выбранный тип транспорта
 * @returns массив доступных статусов выбранного типа транспорта для селекта
 */
export const useStatuses = (transportType: TRANSPORT_TYPE | undefined) => {
  const statuses = useMemo(() => {
    let _statuses: IStatus<string>[] = [];

    switch (transportType) {
      case TRANSPORT_TYPE.TAXI:
      case TRANSPORT_TYPE.BUS:
        _statuses = TAXI_STATUSES;
        break;
      case TRANSPORT_TYPE.CARSHARING:
        _statuses = CARSHARING_STATUSES;
        break;
      case TRANSPORT_TYPE.PERSONAL:
        _statuses = PERSONAL_STATUSES;
        break;
      case TRANSPORT_TYPE.PUBLIC:
        _statuses = PUBLIC_STATUSES;
        break;
      case TRANSPORT_TYPE.GROUP_TRANSFER:
        _statuses = GROUP_TRANSFER_STATUSES;
        break;
      case undefined:
        _statuses = STATUSES;
        break;
      default:
        _statuses = [];
    }
    return _statuses.map(x => {
      const typeDescription = transportType
        ? ''
        : `[${TransportTypeDescriptions[x.name.split('_')[0] as TRANSPORT_TYPE]}]`;

      return {
        label: `${x.rusName} ${typeDescription}`,
        value: x.name,
      };
    });
  }, [transportType]);

  return statuses;
};
