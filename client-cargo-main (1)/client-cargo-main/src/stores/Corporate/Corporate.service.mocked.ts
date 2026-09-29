import { injectable } from 'inversify';

import {
  ICorporateService, IOrganization, IPosition, TDepartment
} from './Corporate.interface';

@injectable()
export class CorporateServiceMocked implements ICorporateService {
  getAllOrganizations = async (): Promise<IOrganization[]> => (await import('mock/stores/Corporate/Organizations/getAllOrganizations.json')).default as IOrganization[];

  getAllDepartments = async (): Promise<TDepartment[]> => (await import('mock/stores/Corporate/Department/getAllDepartments.json')).default as TDepartment[];

  getAllPositions = async (): Promise<IPosition[]> => (await import('mock/stores/Corporate/Position/getAllPositions.json')).default as IPosition[];

  getOrganization = async (): Promise<IOrganization> => (await import('mock/stores/Corporate/Organizations/getOrganization.json')).default as IOrganization;

  getDepartment = async (): Promise<TDepartment> => (await import('mock/stores/Corporate/Department/getDepartment.json')).default as TDepartment;

  addDepartment = async (): Promise<TDepartment> => (await import('mock/stores/Corporate/Department/addDepartment.json')).default as TDepartment;

  editDepartment = async (): Promise<number> => Promise.resolve(200);

  deleteDepartment = async (): Promise<number> => Promise.resolve(200);

  getPosition = async (): Promise<IPosition> => (await import('mock/stores/Corporate/Position/getPosition.json')).default as IPosition;
}
