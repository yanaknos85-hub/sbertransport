import React from 'react';

import { Translation } from 'i18n/ru';

import {
  ContractBaseSVG,
  BudgetSVG,
  TariffsSVG,
  SettingsSVG,
  IndicatorsSVG
} from '../static';
import { Buttons } from '../types/Home.types';
import {
  CONTRACTS,
  LIMITS_SETTINGS,
  ORDER_EXECUTION,
  SERVICE_SETTINGS,
  TARIFFS
} from 'constants/constants.routes';

export const hotButtons: {
  id: Buttons;
  titleKey: keyof Translation['HotButtons'];
  icon: React.FunctionComponent<React.SVGProps<SVGSVGElement>>;
  link: string;
}[] = [
  {
    id: Buttons.MANAGE_CONTRACT_BASE,
    titleKey: 'manageContractBase',
    icon: ContractBaseSVG,
    link: CONTRACTS,
  },
  {
    id: Buttons.BUDGET,
    titleKey: 'budget', icon: BudgetSVG,
    link: LIMITS_SETTINGS,
  },
  {
    id: Buttons.TARIFFS_SETTINGS,
    titleKey: 'tariffsSettings',
    icon: TariffsSVG,
    link: TARIFFS,
  },
  {
    id: Buttons.SERVICE_PARAMS,
    titleKey: 'serviceParamsSettings',
    icon: SettingsSVG,
    link: SERVICE_SETTINGS,
  },
  {
    id: Buttons.INDICATORS,
    titleKey: 'indicators',
    icon: IndicatorsSVG,
    link: ORDER_EXECUTION,
  },
];
