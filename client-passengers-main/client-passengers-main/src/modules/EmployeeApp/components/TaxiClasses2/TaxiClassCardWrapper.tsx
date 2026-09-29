
import { FormInstance } from 'antd';
import { observer } from 'mobx-react';
import React, { Dispatch, SetStateAction } from 'react';

import { LIMIT_TYPE, LimitSharing } from 'stores/Limits/Limit.interface';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import {
  BusEnum,
  ExternalProviderEnum,
  GroupTransferClassesEnum,
  ITaxi,
  ITripTariff,
  TExternalPrices,
  TTaxiClass,
  TaxiClassEnum,
  TaxiEnum
} from 'stores/Trip/Trip.interface';

import { isPrivilegedTaxiClass } from 'utils/trips';

import { getAvailableBalance } from '../CreateTripRequest/utils/utils';
import Process from '../Evaluation/Constants/Process';
import { IProduct } from '../TransportOrder/types';
import { CardContent } from './Card/CardContent';
import { useCardDisabled } from './hooks/useCardDisabled';
import { TripLimit } from './hooks/useTripLimit';

export interface ICardProps {
  config: ITaxi;
  value?: ITripTariff;
  type?: LIMIT_TYPE;
  limitSharing?: LimitSharing;
  isDisabledByPosition?: boolean;
  form?: FormInstance;
  purpose: string;
  externalPrices?: TExternalPrices[];
  externalProvider?: string;
  isBonus?: boolean;
  hasBonus?: boolean;
  economyTaxiCost?: number;
  isAvailableWithoutBonus?: boolean;
  key: string;
  product?: IProduct;
  // bonuses?: Bonuses;
  limitPercentage?: number;
  limitAuthor?: string;

  setShowCarToolTrip?: Dispatch<SetStateAction<number | null>>;
  setProduct?: React.Dispatch<TransportTypeEnum>;
  setStep?: React.Dispatch<number>;
  setSubClass?: (value?: TaxiEnum | BusEnum | GroupTransferClassesEnum) => void;
  setExternalProvider?: (value: ExternalProviderEnum) => void;
  setProcess?: Dispatch<Process>;
  loadExternalPrices?: () => void;
  handleNextStep?: (value?: TTaxiClass) => void;
}

interface ITaxiClassCardProps {
  option: ITaxi;
  tariff?: ITripTariff;
  tariffsInfo: ITripTariff[];
  externalPrices?: TExternalPrices[];
  isEditable: boolean;
  purpose: string;
  tripLimit?: TripLimit;
  form?: FormInstance;
  isExternal?: boolean;
  externalProvider?: ExternalProviderEnum;
  economyTaxiCost?: number;
  availableTaxiClasses?: TaxiClassEnum[];
  // bonuses?: Bonuses;
  product?: IProduct;
  limitPercentage?: number;
  limitAuthor?: string;

  setShowCarToolTrip?: Dispatch<SetStateAction<number | null>>;
  setProduct?: React.Dispatch<TransportTypeEnum>;
  setStep: React.Dispatch<number>;
  setSubClass?: (value?: TaxiEnum | BusEnum | GroupTransferClassesEnum) => void;
  setExternalProvider?: (value: ExternalProviderEnum) => void;
  setProcess?: Dispatch<Process>;
  loadExternalPrices?: () => void;
  handleNextStep?: () => void;
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
    externalProvider,
    economyTaxiCost,
    setShowCarToolTrip,
    availableTaxiClasses = [],
    // bonuses,
    isExternal,
    product,
    limitPercentage,
    limitAuthor,
    setProduct,
    setStep,
    setSubClass,
    setExternalProvider,
    setProcess,
    loadExternalPrices,
    handleNextStep,
  }): JSX.Element => {
    const {
      employeeLimit,
      departmentLimit,
      employeeSharing,
      departmentSharing,
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

    const hasDepartmentLimit = getAvailableBalance(limitDepartmentSharingByTransport) > 0;
    const limitSharingByTransport = hasDepartmentLimit
      ? limitDepartmentSharingByTransport
      : limitEmployeeSharingByTransport;

    const isBonus = isPrivilegedTaxiClass(taxiClass);
    const hasBonus = true;
    const isAvailableWithoutBonus
      = isBonus && taxiClass && !isExternal ? availableTaxiClasses.includes(taxiClass as TaxiClassEnum) : true;

    const props: ICardProps = {
      config: { ...option, disabled: isExternal ? false : !tariff?.limitAvailable },
      type: hasDepartmentLimit ? departmentLimit?.limitType : employeeLimit?.limitType,
      value: tariff,
      key: name,
      limitSharing: limitSharingByTransport,
      isDisabledByPosition: isExternal ? false : isDisabledByPosition,
      form,
      purpose,
      externalPrices,
      externalProvider,
      isBonus,
      hasBonus,
      economyTaxiCost,
      isAvailableWithoutBonus,
      product,
      // bonuses,
      limitPercentage,
      limitAuthor,

      setShowCarToolTrip,
      setProduct,
      setStep,
      setSubClass,
      setExternalProvider,
      setProcess,
      loadExternalPrices,
      handleNextStep,
    };

    return <CardContent props={props} />;
  }
);

export default TaxiClassCardWrapper;
