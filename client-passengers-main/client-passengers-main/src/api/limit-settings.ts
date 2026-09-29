import * as t from 'io-ts';

import { APIQueryResult, useAPI } from 'api';
import { plainToNew } from 'utils';

import { LIMITSETTINGS, MOCKED_API_PREFIX } from 'constants/constants.env';

import { IOLimitColorsPercent, TLimitColorsPercent } from 'stores/Limits/Limit.interface';
import { LimitColorsPercentModel } from 'stores/Limits/Models/LimitColorsPercent.model';

declare module 'api' {
  interface Cache {
    limitColorsPercentInfo: {
      key: ['limitColorsPercentInfo'];
      value: LimitColorsPercentModel[];
    };
  }
}

export const useGetLimitColorsPercentInfo = (): APIQueryResult<LimitColorsPercentModel[], unknown> => useAPI(['limitColorsPercentInfo'], ({ http, process }) => http
  .get<TLimitColorsPercent[]>(`${MOCKED_API_PREFIX}${LIMITSETTINGS}`)
  .then(process.decodeResponseData(t.array(IOLimitColorsPercent)))
  .then(data => plainToNew<LimitColorsPercentModel[]>(LimitColorsPercentModel, data))
);
