
import { FormInstance } from 'antd';
import { observer } from 'mobx-react';
import React, { Dispatch, SetStateAction } from 'react';

import { Bonuses } from 'stores/Limits/Limit.interface';
import {
  ITaxi, ITripTariff, TExternalPrices, TaxiClassEnum
} from 'stores/Trip/Trip.interface';

import { isPrivilegedTaxiClass } from 'utils/trips';

import { getAvailableBalance } from '../CreateTripRequest/utils/utils';
import Process from '../Evaluation/Constants/Process';
import Card from './Card/Card';
import { useCardDisabled } from './hooks/useCardDisabled';
import { TripLimit } from './hooks/useTripLimit';

interface ITaxiClassCardProps {
  option: ITaxi;
  tariff?: ITripTariff;
  tariffsInfo: ITripTariff[];
  externalPrices?: TExternalPrices[];
  isEditable: boolean;
  purpose: string;
  setProcess?: Dispatch<Process>;
  tripLimit?: TripLimit;
  form?: FormInstance;
  isExternal?: boolean;
  externalTaxiClass?: string;
  economyTaxiCost?: number;
  setShowCarToolTrip?: Dispatch<SetStateAction<number | null>>;
  availableTaxiClasses?: TaxiClassEnum[];
  bonuses?: Bonuses;
}

const TaxiClassCardWrapper: React.FC<ITaxiClassCardProps> = observer(
  ({
    option,
    tariff,
    tariffsInfo,
    isEditable,
    form,
    purpose,
    tripLimit,
    externalPrices,
    externalTaxiClass,
    economyTaxiCost,
    setShowCarToolTrip,
    availableTaxiClasses = [],
    isExternal,
    bonuses,
    setProcess,
  }): JSX.Element => {
    const {
      employeeLimit, departmentLimit, employeeSharing, departmentSharing,
    } = tripLimit ?? {};
    const {
      transportType, name, taxiClass,
    } = option;
    const limitEmployeeSharingByTransport = employeeSharing?.find(el => el.transportType === transportType);
    const limitDepartmentSharingByTransport = departmentSharing?.find(el => el.transportType === transportType);

    const { isDisabledByPosition } = useCardDisabled({
      tariffsInfo,
      option,
      isEditable,
      employeeLimit,
      departmentLimit,
      limitEmployeeSharingByTransport,
      limitDepartmentSharingByTransport,
    });

    const hasEmployeeLimit = getAvailableBalance(limitEmployeeSharingByTransport) > 0;
    const limitSharingByTransport = hasEmployeeLimit
      ? limitEmployeeSharingByTransport
      : limitDepartmentSharingByTransport;

    const isBonus = isPrivilegedTaxiClass(taxiClass);
    const hasBonus = true;
    const isAvailableWithoutBonus
      = isBonus && taxiClass && !isExternal ? availableTaxiClasses.includes(taxiClass as TaxiClassEnum) : true;

    return (
      <Card
        config={{ ...option, disabled: isExternal ? false : !tariff?.limitAvailable }}
        type={hasEmployeeLimit ? employeeLimit?.limitType : departmentLimit?.limitType}
        value={tariff}
        key={name}
        limitSharing={limitSharingByTransport}
        isDisabledByPosition={isExternal ? false : isDisabledByPosition}
        form={form}
        purpose={purpose}
        externalPrices={externalPrices}
        externalTaxiClass={externalTaxiClass}
        isBonus={isBonus}
        hasBonus={hasBonus}
        economyTaxiCost={economyTaxiCost}
        setShowCarToolTrip={setShowCarToolTrip}
        isAvailableWithoutBonus={isAvailableWithoutBonus}
        bonuses={bonuses}
        setProcess={setProcess}
      />
    );
  }
);

export default TaxiClassCardWrapper;
