import { EmployeeModel } from '@sber-sbertransport/mf-core';
import { inject, injectable } from 'inversify';
import { debounce } from 'lodash';
import mapKeys from 'lodash/mapKeys';
import { action, observable } from 'mobx';
import { plainToNew } from 'utils';
import { TYPES } from 'ioc/types';
import type { IEmpoloyeeExtStore, IEmpoloyeeExtService } from './EmployeeExt.interface';

@injectable()
export class DIEmployeeExtStore implements IEmpoloyeeExtStore {
  @inject(TYPES.IEmployeeExtService)
  private service!: IEmpoloyeeExtService;

  @observable
    employeeAutocompleteList: EmployeeModel[] = [];

  private clearAutocompleteList(): void {
    this.employeeAutocompleteList = [];
  }

  searchEmployees = debounce(async (params: { fullName: string }) => {
    this.employeeAutocompleteList = observable(await this.searchEmployeesByName(params));
  }, 1500);

  findEmployees(params: { fullName: string }): void {
    if (
      params.fullName.length > 3
      && !this.employeeAutocompleteList.some(x => x.fullNameWithCode === params.fullName)
    ) {
      this.clearAutocompleteList();
      this.searchEmployees(params);
    }
  }

  @action.bound
  async searchEmployeesByName(params: { fullName: string }): Promise<EmployeeModel[]> {
    const { content } = await this.service.searchEmployeesByName(params);
    const models = plainToNew<EmployeeModel[]>(EmployeeModel, content) ?? [];
    const noBlank = models.filter(x => x.fullNameWithCode !== '');
    return Object.values(mapKeys(noBlank, 'fullNameWithCode')); // removes duplicates
  }
}
