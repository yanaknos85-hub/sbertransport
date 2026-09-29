/* eslint-disable @typescript-eslint/no-explicit-any */

import { EmployeeDetailedModel } from '@sber-sbertransport/mf-core';
import * as t from 'io-ts';
import moment from 'moment';
import { useCallback, useMemo } from 'react';

import { DATE_FORMAT } from 'constants/constants.app';

import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import {
  ApproverDTOList,
  LIMIT_REQUEST_LEVEL_TITLES,
  LIMIT_SHARING_TYPE_TITLES,
  LIMIT_TYPE,
  author
} from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export const ISpentActionsType = t.strict({
  id: t.string,
  fullName: tt.optional(t.string),
  transportType: t.string,
  period: t.number,
  sum: tt.money,
  creationTime: t.string,
  approverDtoList: t.array(ApproverDTOList),
  status: t.string,
  limitSum: tt.optional(tt.money),
  humanReadableId: t.string,
  author,
  limitBalance: tt.optional(tt.money),
  limitSharingType: tt.optional(t.string),
  description: tt.optional(t.string),
  limitType: ioTypeFromEnum<LIMIT_TYPE>('LIMIT_TYPE', LIMIT_TYPE),
  limitHumanreadableid: tt.optional(t.string),
  level: tt.optional(t.string),
  declineReason: tt.optional(t.string),
});

export type ISpentActionsType = t.TypeOf<typeof ISpentActionsType>;

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
const getSpentActionsItem = ({
  limit,
  detailedEmployees,
}: {
  limit: any; // FIXME any
  detailedEmployees: Dictionary<EmployeeDetailedModel>;
}) => ({
  // FIXME @typescript-eslint/explicit-function-return-type
  id: limit.humanReadableId,
  transportType: limit.transportType,
  fullName: detailedEmployees[limit.author.id]?.nameWithInitials,
  creationTime: moment(limit.creationTime).format(DATE_FORMAT.MONTH_NAME),
  sum: limit.sum,
  period: limit.period,
  approverDtoList: limit.ApproverDTOList,
  status: limit.status,
  limitSum: limit.limitSum,
  limitBalance: limit.limitBalance,
  humanReadableId: limit.humanReadableId,
  limitHumanreadableid: limit.limitHumanreadableid,
  declineReason: limit.declineReason || '-',
  limitSharingType: LIMIT_SHARING_TYPE_TITLES[limit.limitSharingType as keyof typeof LIMIT_SHARING_TYPE_TITLES],
  description: limit.description,
  limitType: limit.limitType,
  level: LIMIT_REQUEST_LEVEL_TITLES[limit.level as keyof typeof LIMIT_REQUEST_LEVEL_TITLES],
  author: {
    firstName: limit.author.firstName,
    humanReadableId: limit.author.humanReadableId,
    id: limit.author.id,
    lastName: limit.author.lastName,
    patronymic: limit.author?.patronymic,
    personnelNumber: limit.author.personnelNumber,
  } as any,
});

export const useDetailedLimitsMapper = (): {
  getSpentActions: () => ISpentActionsType[];
  getSpentAmountsByTransportType: Record<string, number>;
} => {
  const {
    [StoreNames.limitsStore]: limits,
    [StoreNames.transportTypesStore]: transportTypes,
    [StoreNames.mappedStore]: mappedStore,
  } = useAppStoreContext();

  const detailedEmployees = mappedStore.employeeListByOrgDetailed;

  const getSpentActions = useCallback((): ISpentActionsType[] => {
    const result: ISpentActionsType[] = [];
    if (transportTypes.transportTypes) {
      transportTypes.transportTypes.forEach(transportTypeModel => {
        limits.limitRequestsStats
          .filter(limit => limit.transportType === transportTypeModel.name)
          .forEach((limit: any) => {
            result.push(getSpentActionsItem({ limit, detailedEmployees }));
          });
      });
    }

    return result;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [detailedEmployees, transportTypes.transportTypes, limits.limitRequests]);
  // FIXME react-hooks/exhaustive-deps

  const getSpentAmountsByTransportType = useMemo((): Record<string, number> => {
    const result: Record<string, number> = {};
    const breakdown = getSpentActions();

    if (transportTypes.transportTypes) {
      transportTypes.transportTypes.forEach(transportTypeModel => {
        result[transportTypeModel.id] = breakdown.reduce(
          (acc, item) => (transportTypeModel.name === item.transportType ? acc + Number(item.sum) : acc),
          0
        );
      });
    }

    return result;
  }, [transportTypes.transportTypes, getSpentActions]);

  return { getSpentActions, getSpentAmountsByTransportType };
};
