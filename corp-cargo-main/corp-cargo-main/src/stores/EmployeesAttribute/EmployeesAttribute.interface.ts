/* eslint-disable no-use-before-define */
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export interface IEmployeesAttributeStore {
  employeesAttributes: EmployeesAttribute[];
  employeesAttributesMapped: Dictionary<EmployeesAttribute>;

  refreshEmployeesAttributes(): void;

  initStore(): Promise<void>;

  getEmployeesAttribute(eAttributeId: string): Promise<EmployeesAttribute>;

  createEmployeesAttribute(employeesAttribute: Omit<EmployeesAttribute, 'id'>): Promise<string>;

  updateEmployeesAttribute(employeesAttribute: EmployeesAttribute): Promise<void>;

  deleteEmployeesAttribute(eAttributeId: string): Promise<void>;
}

export interface IEmployeesAttributeService {
  getAllEmployeesAttributes(): Promise<EmployeesAttribute[]>;

  getEmployeesAttribute(eAttributeId: string): Promise<EmployeesAttribute>;

  createEmployeesAttribute(employeesAttribute: Omit<EmployeesAttribute, 'id'>): Promise<EmployeesAttribute>;

  updateEmployeesAttribute(employeesAttribute: EmployeesAttribute): Promise<number>;

  deleteEmployeesAttribute(eAttributeId: string): Promise<number>;
}

export const EmployeesAttribute = t.type({
  id: tt.uuid,
  name: t.string,
  status: t.union([t.literal('ACTIVE'), t.literal('INACTIVE')]),
});

export type EmployeesAttribute = t.TypeOf<typeof EmployeesAttribute>;
