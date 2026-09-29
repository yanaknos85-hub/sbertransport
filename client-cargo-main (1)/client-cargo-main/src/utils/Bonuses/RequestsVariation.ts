import moment from 'moment';
import { emptySign } from 'shared/constants/constants';

import { BonusesRequest } from 'stores/Limits/Limit.interface';

export const requestsVariation = (request: BonusesRequest): BonusesRequest => ({
  ...request,
  updateTime: moment(request.updateTime).format('DD.MM.YYYY'),
  sum: `${request.operation === 'DEPOSIT' ? '+' : emptySign}${(request.sum / 100).toLocaleString('ru-RU')} ₽`,
} as unknown as BonusesRequest);
