import { useEffect, useMemo } from 'react';
import { ServiceEnum } from 'constants/constants.app';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { LimitSharing } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import { getAvailablePercentage } from 'utils/trips';

const useLimits = (): {
  employeeLimitsPercentsByType: Record<ServiceEnum, number>;
  departmentLimitsPercentsByType: Record<ServiceEnum, number>;
} => {
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();
  const { currentEmployeeSharing, currentDepartmentSharing } = limitsStore;

  useEffect(() => {
    limitsStore.getLimits();
  }, [limitsStore]);

  // Персональные лимиты
  const employeeLimitsByType: Record<ServiceEnum, LimitSharing | null> = useMemo(() => ({
    [ServiceEnum.employeeTransportation]: currentEmployeeSharing.find(i => i.transportType === TransportTypeEnum.TAXI) || null,
    [ServiceEnum.cargo]: currentEmployeeSharing.find(i => i.transportType === TransportTypeEnum.DEDICATED) || null,
    // todo удалить после тестирования и обкатки в проме !!!
    // [ServiceEnum.cargoMulti]: currentEmployeeSharing.find((i) => i.transportType === TransportTypeEnum.DEDICATED) || null,
    // [ServiceEnum.regularCargo]: null,
    [ServiceEnum.maintenance]: null,
    [ServiceEnum.parking]: null,
  }), [currentEmployeeSharing]);

  // todo удалить после тестирования и обкатки в проме !!!
  // Пока равен обычной
  // employeeLimitsByType[ServiceEnum.regularCargo] = employeeLimitsByType[ServiceEnum.cargo];

  // Лимиты подразделения
  const departmentLimitsByType: Record<ServiceEnum, LimitSharing | null> = useMemo(() => ({
    [ServiceEnum.employeeTransportation]: null,
    [ServiceEnum.cargo]: currentDepartmentSharing.find(i => i.transportType === TransportTypeEnum.DEDICATED) || null,
    // todo удалить после тестирования и обкатки в проме !!!
    // [ServiceEnum.cargoMulti]: currentDepartmentSharing.find((i) => i.transportType === TransportTypeEnum.DEDICATED) || null,
    // [ServiceEnum.regularCargo]: null,
    [ServiceEnum.maintenance]: null,
    [ServiceEnum.parking]: null,
  }), [currentEmployeeSharing]);

  // todo удалить после тестирования и обкатки в проме !!!
  // Пока равен обычной
  // departmentLimitsByType[ServiceEnum.regularCargo] = departmentLimitsByType[ServiceEnum.cargo];

  const employeeLimitsPercentsByType: Record<ServiceEnum, number> = useMemo(() => ({
    [ServiceEnum.employeeTransportation]: getAvailablePercentage(employeeLimitsByType[ServiceEnum.employeeTransportation]),
    [ServiceEnum.cargo]: getAvailablePercentage(employeeLimitsByType[ServiceEnum.cargo]),
    // todo удалить после тестирования и обкатки в проме !!!
    // [ServiceEnum.cargoMulti]: getAvailablePercentage(employeeLimitsByType[ServiceEnum.cargo]),
    // [ServiceEnum.regularCargo]: 0,
    [ServiceEnum.maintenance]: 0,
    [ServiceEnum.parking]: 0,
  }), [employeeLimitsByType]);

  const departmentLimitsPercentsByType: Record<ServiceEnum, number> = useMemo(() => ({
    [ServiceEnum.employeeTransportation]: 0,
    [ServiceEnum.cargo]: getAvailablePercentage(departmentLimitsByType[ServiceEnum.cargo]),
    // todo удалить после тестирования и обкатки в проме !!!
    // [ServiceEnum.cargoMulti]: getAvailablePercentage(departmentLimitsByType[ServiceEnum.cargo]),
    // [ServiceEnum.regularCargo]: getAvailablePercentage(departmentLimitsByType[ServiceEnum.regularCargo]),
    [ServiceEnum.maintenance]: 0,
    [ServiceEnum.parking]: 0,
  }), [departmentLimitsByType]);

  return {
    employeeLimitsPercentsByType,
    departmentLimitsPercentsByType,
  };
};

export default useLimits;
