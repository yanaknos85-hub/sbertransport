import { useEffect, useState } from 'react';
import { useUserAvailableClasses } from 'shared/hooks/useUserAvailableClasses';

import { Limit, LimitSharing } from 'stores/Limits/Limit.interface';
import { LimitModel } from 'stores/Limits/Models/LimitModel';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { ITaxi, ITripTariff, TaxiClassEnum } from 'stores/Trip/Trip.interface';
import { taxiClassIsClassic } from 'utils/trips';

import { useTaxiTariffs } from './useTaxiTariffs';

const getDisableReason = ({
  option,
  limitSharingByTransport,
  isMonthLimitExceeded,
  isOverallLimitExceeded,
  isDisabledByPosition,
  isEditable,
  tariffExist,
  disabledByBonuses,
}: {
  option: ITaxi;
  limitSharingByTransport: LimitSharing | undefined;
  isMonthLimitExceeded: boolean;
  isOverallLimitExceeded: boolean;
  isDisabledByPosition: boolean;
  isEditable: boolean;
  tariffExist: boolean;
  disabledByBonuses: boolean;
}): string => {
  let errorMsg = `Причина отключения ВТ: ${option.name} ${option.transportType} ${option.taxiClass}. `;
  if (limitSharingByTransport === undefined) {
    errorMsg += `Отсутствует распределение для данного вида транспорта (${option.transportType}). `;
  }
  if (isMonthLimitExceeded) {
    errorMsg += 'Превышен месячный лимит. ';
  }
  if (isOverallLimitExceeded) {
    errorMsg += 'Превышен общий лимит';
  }
  if (isDisabledByPosition) {
    errorMsg += 'Отключён по должности. ';
  }
  if (!isEditable) {
    errorMsg += 'Редактирование карточки ВТ отключено. ';
  }
  if (!tariffExist) {
    errorMsg += 'Не существует тарифа. ';
  }
  if (disabledByBonuses) {
    errorMsg += 'Бонусов не хватает для поездки сверх лимита';
  }

  return errorMsg;
};

export const useCardDisabled = ({
  tariffsInfo,
  option,
  isEditable,
  employeeLimit,
  departmentLimit,
  limitEmployeeSharingByTransport,
  limitDepartmentSharingByTransport,
  bonusSum,
}: {
  tariffsInfo: ITripTariff[];
  option: ITaxi;
  isEditable: boolean;
  employeeLimit: Limit | LimitModel | undefined;
  departmentLimit: Limit | LimitModel | undefined;
  limitEmployeeSharingByTransport: LimitSharing | undefined;
  limitDepartmentSharingByTransport: LimitSharing | undefined;
  bonusSum?: number;
}): {
  disabled: boolean;
  isDisabledByPosition: boolean;
} => {
  const [disabled, setDisabled] = useState(true);
  const { taxiClass } = option;
  const { tariff } = useTaxiTariffs({ tariffsInfo, option });
  // FIXME сейчас используется доступный транспорт текущего пользователя
  // нужно использовать доступный транспорт, автора заявки (если она есть)
  const { availableClasses } = useUserAvailableClasses();
  const isClassicTaxi = taxiClassIsClassic(taxiClass as TaxiClassEnum);
  const isPublicTaxiClass = taxiClass === TaxiClassEnum.PUBLIC;

  // TODO должен ли использоваться limit?.sum ?? может уже нужно ориентироваться только на limitSharingBalance
  const isOverallLimitExceeded
    = !!(tariff && employeeLimit?.sum && tariff.cost > employeeLimit?.sum)
    || !!(tariff && departmentLimit?.sum && tariff.cost > departmentLimit?.sum);

  const limitSharingByTransport = limitEmployeeSharingByTransport ?? limitDepartmentSharingByTransport;

  const limitSharingBalance = limitSharingByTransport?.limitSharingPerPeriodDTO?.balance;
  const tariffCostMoreLimitSharingBalance = limitSharingBalance ? (tariff?.cost || 0) > limitSharingBalance : true;

  // Вариант со списанием бонусов
  const { tariff: economyTariff } = useTaxiTariffs({
    tariffsInfo,
    option: {
      taxiClass: 'ECONOMY', transportType: TransportTypeEnum.TAXI, waitingTime: 0, name: '',
    },
  });
  const disabledByBonuses
    = (taxiClass === TaxiClassEnum.COMFORT || taxiClass === TaxiClassEnum.BUSINESS) && limitSharingBalance
      ? (tariff?.cost || 0) > limitSharingBalance - (economyTariff?.cost || 0) - (bonusSum || 0)
      : true;

  const isDisabledByPosition = !!(
    taxiClass
    && !availableClasses?.includes(taxiClass)
    && isClassicTaxi
    && disabledByBonuses
  );
  const isMonthLimitExceeded = !!(
    limitSharingByTransport
    && tariff
    && tariffCostMoreLimitSharingBalance
    && disabledByBonuses
  );
  // TODO simplify
  const tariffExist = tariff !== undefined;

  // const disabled = getIsDisabled();
  useEffect(() => {
    const getIsDisabled = (): boolean => {
      const disableByCommonParams
        = limitSharingByTransport === undefined
        || isMonthLimitExceeded
        || isOverallLimitExceeded
        || isDisabledByPosition
        || !isEditable;
      return isPublicTaxiClass ? disableByCommonParams : !tariffExist || disableByCommonParams;
    };

    const isDisabled = getIsDisabled();
    setDisabled(isDisabled);
    if (isDisabled) {
      // eslint-disable-next-line no-console
      console.log(
        getDisableReason({
          option,
          limitSharingByTransport,
          isMonthLimitExceeded,
          isOverallLimitExceeded,
          isDisabledByPosition,
          isEditable,
          tariffExist,
          disabledByBonuses,
        })
      );
    }
  }, [
    limitSharingByTransport,
    isMonthLimitExceeded,
    isOverallLimitExceeded,
    isDisabledByPosition,
    isPublicTaxiClass,
    tariffExist,
    isEditable,
    option,
  ]);

  return { disabled, isDisabledByPosition };
};
