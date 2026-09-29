import { CORP_URL } from '../constants/constants.corp';

export function getUrlCorpApp(): string {
  const url = window.location.href;

  if (url.includes('sbertransport.delta')) {
    return CORP_URL.ADMIN_DELTA;
  }
  if (url.includes('sbertransport.ca')) {
    return CORP_URL.ADMIN_ALFA;
  }
  return CORP_URL.ADMIN_SIGMA;
}
