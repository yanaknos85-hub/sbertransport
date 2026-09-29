import { injectable } from 'inversify';

import { ISpentActionsType } from 'modules/LimitsPage/useDetailedLimitsMapper';

import {
  DepSiblings, ILimitsService, Limit, LimitCostHistory, LimitTransferHistory
} from './Limit.interface';

@injectable()
export class LimitsServiceMocked implements ILimitsService {
  getDepartmentLimits = async (): Promise<Limit[]> => (await import('mock/stores/Limits/getDepartmentLimits.json')).default as any as Limit[];

  getDepartmentLimitsByYear = async (): Promise<Limit> => (await import('mock/stores/Limits/getDepartmentLimitsByYear.json')).default as any as Limit;

  getEmployeeLimits = async (): Promise<Limit[]> => [];

  getLimitSharing = async (): Promise<any> => [];

  getEmployeeLimit = async (): Promise<Limit> => (await import('mock/stores/Limits/getDepartmentLimits.json')).default[0] as any as Limit;

  getLimitsRequestsByAuthor = async (): Promise<ISpentActionsType[]> => [];

  getLimitsRequestByApprover = async (): Promise<any[]> => [];

  getActiveLimitsRequestByApprover = async (): Promise<any> => [];

  getOldLimitsRequestByApprover = async (): Promise<any> => [];

  getAllLimitRequests = async (): Promise<any[]> => [];

  getLimitRequest = async (): Promise<any> => [];

  cancelLimitRequest = async (): Promise<number> => 200;

  approveLimitRequest = async (): Promise<number> => 200;

  changeDepLimitRequest = async (): Promise<number> => 200;

  changeEmpLimitRequest = async (): Promise<number> => 200;

  getLimitByDepartment = async (): Promise<Limit[]> => [];

  getDepSiblings = async (): Promise<DepSiblings[]> => [];

  getLimitTransferHistory = async (): Promise<LimitTransferHistory[]> => [];

  getLimitCostHistory = async (): Promise<LimitCostHistory[]> => [];

  getAccountBonuses = async (): Promise<any> => [];

  getLimitChildren = async (): Promise<any> => [];

  getLimitResponsibles = async (): Promise<any> => [];
}
