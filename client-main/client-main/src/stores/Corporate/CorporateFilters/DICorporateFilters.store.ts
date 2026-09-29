import { inject, injectable } from 'inversify';
import { action, computed, observable } from 'mobx';

import { TYPES } from 'ioc/types';

import { DepartmentDetailedModel } from 'stores/Corporate/models/DepartmentDetailed.model';
import { MappedStore } from 'stores/Mapped/DIMapped.store';
import { includesByLowerCaseAndSpaces } from 'utils';

import { ICorporateFiltersStore, TDepartmentFiltersTitles } from './CorporateFilters.interface';

@injectable()
export class DICorporateFiltersStore implements ICorporateFiltersStore {
  @inject(TYPES.MappedStore)
  private mappedStore!: MappedStore;

  @observable
    filters = new Map<TDepartmentFiltersTitles, string>();

  @computed
  get departmentsFiltered(): DepartmentDetailedModel[] {
    let result: DepartmentDetailedModel[] = [];

    const data = Object.values(this.mappedStore.departmentsListDetailed);

    this.filters.forEach((value, key) => {
      if (result.length > 0) {
        result = result.filter(dep => includesByLowerCaseAndSpaces(dep[key], value));
      } else {
        // @ts-ignore
        result = data.filter(dep => includesByLowerCaseAndSpaces(dep[key], value));
      }
    });

    // @ts-ignore
    return this.filters.size === 0 && result.length === 0 ? data : result;
  }

  @action.bound
  editFilteres(filters: Map<TDepartmentFiltersTitles, string>): void {
    filters.forEach((filter, key) => this.filters.set(key, filter));
  }

  @action.bound
  refreshFilteres(): void {
    this.filters = new Map();
  }
}
