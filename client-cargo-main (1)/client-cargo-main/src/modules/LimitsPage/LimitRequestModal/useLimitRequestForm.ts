import { useEffect } from 'react';
import { FormInstance } from 'antd/lib/form/Form';
import moment, { Moment } from 'moment';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { TDepartment } from 'stores/Corporate/Corporate.interface';
import { LIMIT_TYPE } from 'stores/Limits/Limit.interface';
import { SiblingsParams } from 'stores/Limits/LimitsRequest.interface';
import { StoreNames } from 'stores/StoreNames.enum';
import { UUID } from 'utils/io-ts';
import { ISpentActionsType } from 'modules/LimitsPage/useDetailedLimitsMapper';

import { getEmpInitials } from '../../ApprovalPage/LimitRequestPage/LimitRequestDetailedView/LimitRequestDetailedComponents/util';
import { LimitRequestLevel, siblingsLimitMoneyPercent } from './constants';

export interface InitialRequestData {
  id?: string;
  transportType?: string;
  fullName?: string;
  creationTime?: Moment;
  sum?: number;
  status?: string;
  limitSum?: number;
  limitBalance?: number;
  humanReadableId?: string;
  limitHumanreadableid?: string;
  description?: string;
  period?: string;
  level?: string;
  departments?: string[] | UUID[];
}

export const useLimitRequestForm = (
  isEdit = false,
  setDP: (options: any[]) => void,
  formRef: FormInstance,
  request?: ISpentActionsType,
  monthArray?: any[]
): {
    onFinish: () => void;
    initialRequestValues: InitialRequestData;
  } => {
  // FIXME sonarjs/cognitive-complexity
  const {
    [StoreNames.employeeStore]: employeeStore,
    [StoreNames.corporateStore]: corporateStore,
    [StoreNames.limitsRequestStore]: requestStore,
    [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();

  const departments: string[] = [];

  const isSiblings: boolean = request?.limitType === LIMIT_TYPE.DEPARTMENT && request.approverDtoList?.length > 1;

  const onFinish = (): void => {
    formRef.resetFields();
  };

  const setDepType = (): void => {
    const options = requestStore.siblings.map(department => {
      departments.push(department.id);
      return {
        value: department.id,
        label: department.departmentName,
      };
    });
    if (options.length > 0) {
      setDP(options);
    }
  };

  const initialRequestValues: InitialRequestData = {};

  // eslint-disable-next-line no-shadow
  const getDepartments = (request: ISpentActionsType): string[] => {
    // FIXME no-shadow
    const result = [];
    if (isSiblings) {
      corporateStore.departments.forEach((dep: TDepartment): void => {
        request.approverDtoList.forEach((element: { departmentId: string }): void => {
          if (element.departmentId === dep.id) {
            departments.push(dep.id);
          }
        });
      });
    }
    // @ts-ignore
    result.push(...departments);
    return result;
  };

  useEffect(() => {
    if (request?.limitType === LIMIT_TYPE.DEPARTMENT) {
      const data = {
        departmentId: selfStore.depId,
        percent: siblingsLimitMoneyPercent,
        transportType: request.transportType,
        year: new Date().getFullYear(),
        sum: request.sum,
      };
      requestStore.getDepSiblings(data as SiblingsParams).then(() => {
        setDepType();
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [request]);
  // FIXME react-hooks/exhaustive-deps

  if (isEdit && request?.id) {
    initialRequestValues.id = request.humanReadableId;
    initialRequestValues.transportType = request.transportType;
    initialRequestValues.fullName = getEmpInitials(request.author.id, employeeStore);
    initialRequestValues.creationTime = moment(request.creationTime);
    initialRequestValues.sum = request.sum;
    initialRequestValues.status = request.status;
    initialRequestValues.limitSum = request.limitSum;
    initialRequestValues.limitBalance = request.limitBalance;
    initialRequestValues.humanReadableId = request.humanReadableId;
    initialRequestValues.limitHumanreadableid = request.limitHumanreadableid;
    initialRequestValues.description = request.description;
    initialRequestValues.period = monthArray && monthArray[0].value;
    initialRequestValues.level = (isSiblings && LimitRequestLevel.SIBLINGS) || LimitRequestLevel.PARENT;
    initialRequestValues.departments = isSiblings ? getDepartments(request) : [];
  }

  return {
    onFinish,
    initialRequestValues,
  };
};
