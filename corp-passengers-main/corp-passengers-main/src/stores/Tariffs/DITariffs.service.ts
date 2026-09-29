import { injectable, inject } from 'inversify';
import { TYPES } from 'ioc/ioc.types';
import { type IHttpService, type ResponseService } from '@sber-sbertransport/mf-core';
import { ITariffsService, TariffFilter, TariffsInfo } from 'stores/Tariffs/Tariffs.interface';
import { GET_ALL_TARIFFS, SEARCH } from 'constants/constants.api';

@injectable()
export class DITariffsService implements ITariffsService {
  @inject(TYPES.IHttpService)
  private http!: IHttpService;

  @inject(TYPES.IResponseService)
  private process!: ResponseService;

  fetchFilteredTariffs = async (params: TariffFilter) => {
    return this.http.post<TariffsInfo>(`${GET_ALL_TARIFFS}${SEARCH}?size=${params.page.pageSize}&page=${params.page.pageNumber}`, {
      ...params,
    }).then(this.process.decodeResponseData(TariffsInfo));
  };
}
