/* eslint-disable react-hooks/exhaustive-deps */
import * as R from 'ramda';

import { ServiceEnum } from 'constants/constants.app';

import { chooseColorByPercent, filterColorsValues } from 'shared/components/LimitBar/LimitBar';
import { LIMIT_TYPE } from 'stores/Limits/Limit.interface';

const DATA = [
  {
    name: 'EMP_LIMIT_GREEN_FROM',
    value: '60',
  },
  {
    name: 'EMP_LIMIT_GREEN_UNTIL',
    value: '100',
  },
  {
    name: 'EMP_LIMIT_YELLOW_FROM',
    value: '30',
  },
  {
    name: 'EMP_LIMIT_YELLOW_UNTIL',
    value: '60',
  },
  {
    name: 'EMP_LIMIT_RED_FROM',
    value: '0',
  },
  {
    name: 'EMP_LIMIT_RED_UNTIL',
    value: '30',
  },
  {
    name: 'DEP_LIMIT_GREEN_FROM',
    value: '60',
  },
  {
    name: 'DEP_LIMIT_GREEN_UNTIL',
    value: '100',
  },
  {
    name: 'DEP_LIMIT_YELLOW_FROM',
    value: '30',
  },
  {
    name: 'DEP_LIMIT_YELLOW_UNTIL',
    value: '60',
  },
  {
    name: 'DEP_LIMIT_RED_FROM',
    value: '0',
  },
  {
    name: 'DEP_LIMIT_RED_UNTIL',
    value: '30',
  },
  {
    name: 'DEP_LIMIT_REMAINS_TARGET',
    value: 'TO_RESERVE',
  },
  {
    name: 'EMP_LIMIT_REMAINS_TARGET',
    value: 'TO_RESERVE',
  },
];

const useColors = (
  employeeLimitsPercentsByType: Record<ServiceEnum, number>
): { colorByType: Record<ServiceEnum, string> } => {
  // Какой то лишний запрос на сервер
  // const { data: limitColorsPercentValues } = useGetLimitColorsPercentInfo();
  const limitColorsPercentValues = DATA;

  const currentTypeColorsPercentValues = filterColorsValues(LIMIT_TYPE.DEPARTMENT, limitColorsPercentValues);

  const colorsPercent = R.mergeAll(
    currentTypeColorsPercentValues.map(element => ({
      [element.name as string]: element.value,
    }))
  );

  const colorsObjectSpecified: any = {};
  const shortObjectsKeys = (localValue: string, key: string): void => {
    // key is being shorted to be the same for personal and department limitType of limit -
    // the end of the string (beginning from 11 index) is the same for both of them
    //  for example EMP_LIMIT_ or DEP_LIMIT_
    // eslint-disable-next-line no-param-reassign
    key = key.substring('EMP_LIMIT_'.length, key.length);
    // FIXME no-param-reassign
    colorsObjectSpecified[key] = localValue;
  };

  R.forEachObjIndexed(shortObjectsKeys as any, colorsPercent);

  const colorByType: Record<ServiceEnum, string> = {
    [ServiceEnum.employeeTransportation]: chooseColorByPercent(
      employeeLimitsPercentsByType[ServiceEnum.employeeTransportation] ?? 0,
      colorsObjectSpecified
    ),
    [ServiceEnum.cargo]: chooseColorByPercent(0, colorsObjectSpecified),
    // todo удалить после тестирования и обкатки в проме !!!
    // [ServiceEnum.cargoMulti]: chooseColorByPercent(0, colorsObjectSpecified),
    // [ServiceEnum.regularCargo]: chooseColorByPercent(0, colorsObjectSpecified),
    [ServiceEnum.maintenance]: chooseColorByPercent(0, colorsObjectSpecified),
    [ServiceEnum.parking]: chooseColorByPercent(0, colorsObjectSpecified),
  };

  return { colorByType };
};

export default useColors;
