import './override.scss';

import * as React from 'react';
import { Dispatch, FC, SetStateAction } from 'react';
import { FormInstance } from 'antd';
import { observer } from 'mobx-react';

import { LIMIT_TYPE, LimitSharing } from 'stores/Limits/Limit.interface';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { ITaxi, ITripTariff, TExternalPrices } from 'stores/Trip/Trip.interface';

import { CardContent } from './CardContent';
import { PublicTransportCompensationsModal } from './PublicTransportCompensationsModal/PublicTransportCompensationsModal';

export interface IUseCardProps {
  name: string;
}

export interface ICardProps {
  config: ITaxi;
  value?: ITripTariff;
  type?: LIMIT_TYPE;
  limitSharing?: LimitSharing;
  isDisabledByPosition?: boolean;
  form?: FormInstance;
  purpose: string;
  externalPrices?: TExternalPrices[];
  externalTaxiClass?: string;
  isBonus?: boolean;
  hasBonus?: boolean;
  economyTaxiCost?: number;
  setShowCarToolTrip?: Dispatch<SetStateAction<number | null>>;
}

const Card: FC<ICardProps> = observer((props): JSX.Element => {
  const { config } = props;
  const { transportType } = config;
  const isPublicTransport = transportType === TransportTypeEnum.PUBLIC;

  return isPublicTransport ? <PublicTransportCompensationsModal props={props} /> : <CardContent props={props} />;
});

export default Card;
