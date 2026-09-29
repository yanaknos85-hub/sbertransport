import React, { Dispatch, SetStateAction, useEffect } from 'react';
import { FormInstance } from 'antd';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { StoreNames } from 'stores/StoreNames.enum';
import {
  ITaxi, ITripTariff, TaxiClassEnum,
  TExternalPrices
} from 'stores/Trip/Trip.interface';

import Card from './Card/Card';
import { useTaxiTariffs } from './hooks';
import { useCardDisabled } from './hooks/useCardDisabled';
import { TripLimit } from './hooks/useTripLimit';

interface ITaxiClassCardProps {
  option: ITaxi;
  tariffsInfo: ITripTariff[];
  externalPrices?: TExternalPrices[];
  isEditable: boolean;
  purpose: string;
  tripLimit?: TripLimit;
  form?: FormInstance;
  isExternal?: boolean;
  externalTaxiClass?: string;
  economyTaxiCost?: number;
  setShowCarToolTrip?: Dispatch<SetStateAction<number | null>>;
}

const TaxiClassCardWrapper: React.FC<ITaxiClassCardProps> = observer(
  ({
    option,
    tariffsInfo,
    isEditable,
    form,
    purpose,
    tripLimit,
    isExternal,
    externalPrices,
    externalTaxiClass,
    economyTaxiCost,
    setShowCarToolTrip,
  }): JSX.Element => {
    const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();
    const {
      employeeLimit, departmentLimit, employeeSharing, departmentSharing,
    } = tripLimit ?? {};
    const {
      transportType, name, taxiClass,
    } = option;
    const { tariff } = useTaxiTariffs({ tariffsInfo, option });
    const limitEmployeeSharingByTransport = employeeSharing?.find(el => el.transportType === transportType);
    const limitDepartmentSharingByTransport = departmentSharing?.find(el => el.transportType === transportType);

    const { disabled, isDisabledByPosition } = useCardDisabled({
      tariffsInfo,
      option,
      isEditable,
      employeeLimit,
      departmentLimit,
      limitEmployeeSharingByTransport,
      limitDepartmentSharingByTransport,
    });

    const classDisabled = isExternal ? false : disabled;
    const limitSharingByTransport = limitEmployeeSharingByTransport ?? limitDepartmentSharingByTransport;

    useEffect(() => {
      limitsStore.getAccountBonuses();
    }, [limitsStore]);

    const isBonus = taxiClass === TaxiClassEnum.COMFORT || taxiClass === TaxiClassEnum.BUSINESS;
    const hasBonus = true;

    return (
      <Card
        config={{ ...option, disabled: classDisabled }}
        type={employeeLimit?.limitType}
        value={tariff}
        key={name}
        limitSharing={limitSharingByTransport}
        isDisabledByPosition={isDisabledByPosition}
        form={form}
        purpose={purpose}
        externalPrices={externalPrices}
        externalTaxiClass={externalTaxiClass}
        isBonus={isBonus}
        hasBonus={hasBonus}
        economyTaxiCost={economyTaxiCost}
        setShowCarToolTrip={setShowCarToolTrip}
      />
    );
  }
);

export default TaxiClassCardWrapper;
