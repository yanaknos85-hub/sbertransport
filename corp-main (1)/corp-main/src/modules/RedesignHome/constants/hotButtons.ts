import { Translation } from 'i18n/ru';

import { Buttons } from '../types/Home.types';
import {
  ANALYTICAL_REPORTING,
  BUSINESS_REPORTS,
  CONTRACTS,
  CORPORATE_ADDRESSES,
  LIMITS_SETTINGS,
  MANAGE_EMPLOYEES,
  SERVICE_SETTINGS,
  TARIFFS
} from 'constants/constants.routes';
import ManageContractBase from 'shared/icons/iconsPopularServices/ManageContractBase.png';
import ReallocateBudget from 'shared/icons/iconsPopularServices/ReallocateBudget.png';
import SettingTariffs from 'shared/icons/iconsPopularServices/SettingTariffs.png';
import ConfigureService from 'shared/icons/iconsPopularServices/ConfigureService.png';
import ManageEmployees from 'shared/icons/iconsPopularServices/ManageEmployees.png';
import AnalyticalReporting from 'shared/icons/iconsPopularServices/AnalyticalReporting.png';
import BusinessReports from 'shared/icons/iconsPopularServices/BusinessReports.png';
import CorporateAddresses from 'shared/icons/iconsPopularServices/CorporateAddresses.png';

export const hotButtons: {
  id: Buttons;
  titleKey: keyof Translation['RedesignHotButtons'];
  icon: string;
  link: string;
}[] = [
  {
    id: Buttons.MANAGE_CONTRACT_BASE,
    titleKey: 'manageContractBase',
    icon: ManageContractBase,
    link: CONTRACTS,
  },
  {
    id: Buttons.BUDGET,
    titleKey: 'budget',
    icon: ReallocateBudget,
    link: LIMITS_SETTINGS,
  },
  {
    id: Buttons.TARIFFS_SETTINGS,
    titleKey: 'tariffsSettings',
    icon: SettingTariffs,
    link: TARIFFS,
  },
  {
    id: Buttons.SERVICE_PARAMS,
    titleKey: 'serviceParamsSettings',
    icon: ConfigureService,
    link: SERVICE_SETTINGS,
  },
  {
    id: Buttons.MANAGE_EMPLOYEES,
    titleKey: 'manageEmployeesSettings',
    icon: ManageEmployees,
    link: MANAGE_EMPLOYEES,
  },
  {
    id: Buttons.ANALYTICAL_REPORTING,
    titleKey: 'analyticalReporting',
    icon: AnalyticalReporting,
    link: ANALYTICAL_REPORTING,
  },
  {
    id: Buttons.BUSINESS_REPORTS,
    titleKey: 'businessReports',
    icon: BusinessReports,
    link: BUSINESS_REPORTS,
  },
  {
    id: Buttons.CORPORATE_ADDRESSES,
    titleKey: 'corporateAddresses',
    icon: CorporateAddresses,
    link: CORPORATE_ADDRESSES,
  },
];
