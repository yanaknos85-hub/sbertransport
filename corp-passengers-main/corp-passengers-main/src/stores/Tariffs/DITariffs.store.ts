import { injectable, inject } from 'inversify';
import { action, observable } from 'mobx';
import { TYPES } from 'ioc/ioc.types';
import {
  ITariffsStore, Tariff, TariffFilter
} from 'stores/Tariffs/Tariffs.interface';
import type { ITariffsService } from 'stores/Tariffs/Tariffs.interface';
import { TariffTypes } from 'constants/constants.app';

@injectable()
export class DITariffsStore implements ITariffsStore {
  @inject(TYPES.ITariffsService)
  private service!: ITariffsService;

  @observable
    isLoading = false;

  @observable
    filteredTariffs: Tariff[] = [];

  @observable
    tariffsFilters!: Omit<TariffFilter, 'tariffType'>;

  @observable
    totalElements = 0;

  @action.bound
  fetchFilteredTariffs(tariffType?: TariffTypes) {
    return this.service.fetchFilteredTariffs({
      ...this.tariffsFilters,
      contractType: tariffType,
    }).then(data => {
      this.filteredTariffs = data.content;
      this.totalElements = data.totalElements;
    });
  }

  @action.bound
  setTariffsFilters(filters: Omit<TariffFilter, 'tariffType'>) {
    this.tariffsFilters = filters;
  }

  @action.bound
  getFilteredTariffs() {
    this.isLoading = true;
    this.fetchFilteredTariffs().finally(() => {
      this.isLoading = false;
    });
  }

  @action.bound
  getSDOTariffs(tariffType: TariffTypes) {
    this.isLoading = true;
    this.fetchFilteredTariffs(tariffType).finally(() => {
      this.isLoading = false;
    });
  }

  @action.bound
  refetchFilteredTariffs() {
    this.fetchFilteredTariffs();
  }

  @action.bound
  refetchSDOTariffs(tariffType: TariffTypes) {
    this.fetchFilteredTariffs(tariffType);
  }
}
