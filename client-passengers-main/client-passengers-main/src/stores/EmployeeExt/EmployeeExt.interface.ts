import { Employee } from '@sber-sbertransport/mf-core';

export interface IEmpoloyeeExtStore {
  findEmployees(params: { fullName: string }): void;
  searchEmployeesByName(params: { fullName: string }): void;
}

export interface IEmpoloyeeExtService {
  searchEmployeesByName(params: { fullName: string }): Promise<{ content: Employee[] }>;
}
