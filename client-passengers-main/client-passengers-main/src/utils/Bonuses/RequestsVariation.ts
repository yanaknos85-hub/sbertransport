import moment from 'moment';

import { BonusesRequest } from 'stores/Limits/Limit.interface';

export const requestsVariation = (request: BonusesRequest): BonusesRequest => ({
  ...request,
  updateTime: moment(request.updateTime).format('DD.MM.YYYY'),
  sum: `${request.operation === 'DEPOSIT' ? '+' : '-'}${(request.sum / 100).toLocaleString()} ₽`,
} as unknown as BonusesRequest);
