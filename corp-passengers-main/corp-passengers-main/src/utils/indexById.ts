import * as R from 'ramda';

const indexById: <T extends { id: string }>(xs: T[]) => Record<string, T> = R.indexBy(R.prop('id'));

export const indexByName: <T extends { name: string }>(xs: T[]) => Record<string, T> = R.indexBy(R.prop('name'));

export default indexById;
