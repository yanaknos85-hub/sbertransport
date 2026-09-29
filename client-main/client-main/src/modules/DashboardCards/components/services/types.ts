import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { ServiceEnum, ServiceTypeEnum } from '../../../../constants/constants.app';

export interface Service {
  hidden?: boolean;
  serviceType?: ServiceTypeEnum;
  transportTypeForLimitSelect?: TransportTypeEnum;
  title: string;
  price: number;
  buttonText: string;
  buttonIsDisabled?: boolean;
  employeeLimitPercent: number;
  departmentLimitPercent: number;
  color: string;
  link: string;
  icon: JSX.Element;
  content?: JSX.Element;
  userPermissions?: string[];
  allowedPermissions?: string[];
  hiddenLimit?: boolean;
}

type OmitServices = Omit<Service, 'title' | 'employeeLimitPercent' | 'departmentLimitPercent' | 'color'> & {
  title?: Service['title'];
  employeeLimitPercent?: Service['employeeLimitPercent'];
  departmentLimitPercent?: Service['departmentLimitPercent'];
  color?: Service['color'];
};

export type TServices = Record<ServiceEnum, OmitServices>;
