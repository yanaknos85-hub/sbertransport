import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

const author = t.strict({
  id: t.string,
  humanReadableId: t.string,
  firstName: t.string,
  lastName: t.string,
  patronymic: t.string,
  personnelNumber: t.string,
});

export const LimitRequestByAuthor = t.strict({
  id: t.string,
  humanReadableId: t.string,
  year: t.number,
  author,
  period: t.number,
  transportType: t.string,
  sum: tt.money,
  description: t.string,
  status: t.string,
  creationTime: t.string,
  limitType: t.string,
});

export type LimitRequestByAuthor = t.TypeOf<typeof LimitRequestByAuthor>;
