import { RequestBodyPublic, RequestBodyPublicParams } from 'modules/PublicRegistry/types/types';
import { reportParams } from '../types/types';

export const processRequestParamsOnXlsDownloadYandexTaxi = (
  params: reportParams,
  organizationId: string
): RequestBodyPublic => {
  const {
    filters, withFilters,
  } = params;
  if (filters && withFilters) {
    const preparedFilters = {
      ...filters,
      organizationId,
    } as Partial<RequestBodyPublicParams>;

    // eslint-disable-next-line no-console
    console.log('public:', preparedFilters);

    const requestBody = {
      withFilters,
      // filters: b64EncodeUnicode(JSON.stringify(preparedFilters)),
      ...preparedFilters,
    } as RequestBodyPublic;

    // reportType === 'COMPENSATIONS' && delete requestBody.withView;

    return requestBody;
  }

  //   return reportType === 'COMPENSATIONS'
  //     ? { withFilters, organizationId }
  //     : {
  //       withFilters,
  //       organizationId,
  //     };

  return {
    withFilters, organizationId,
  };
};
