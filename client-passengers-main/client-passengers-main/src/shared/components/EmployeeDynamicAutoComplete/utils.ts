import { RequestParams } from '@sber-sbertransport/mf-core';

export const paramsGetter = (search: string, extraParams?: RequestParams): RequestParams => {
  const param = /^[0-9]+$/.test(search) ? 'personnelNumber' : 'fullName';
  return { ...(extraParams || {}), [param]: search };
};
