// Filter.store.ts
import { action, observable } from 'mobx';
import * as types from './types/types';

export class FilterStore {
  // Текущие фильтры в форме
  @observable
    currentFilters: types.FilterRequest | null = null;

  // Флаг режима редактирования (повторения)
  @observable
    isEditMode = false;

  constructor() {
    // В MobX 5 ничего дополнительного в конструкторе не требуется
  }

  // Установка фильтров для редактирования
  @action
  setFiltersForEdit(filters: types.FilterRequest) {
    this.currentFilters = filters;
    this.isEditMode = true;
  }

  // Сброс состояния редактирования
  @action
  resetEditMode() {
    this.isEditMode = false;
    this.currentFilters = null;
  }

  // Обновление текущих фильтров
  @action
  updateCurrentFilters(filters: Partial<types.FilterRequest>) {
    this.currentFilters = {
      ...(this.currentFilters || {}),
      ...filters,
    } as types.FilterRequest;
  }
}

export default FilterStore;
