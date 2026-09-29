import * as R from 'ramda';

/**
 * Индексирует массив объектов по их id
 * @param xs - массив объектов с полем id
 * @returns объект, где ключи - id, значения - объекты
 */
const indexById: <T extends { id: string }>(xs: T[]) => Record<string, T> = R.indexBy(R.prop('id'));

export default indexById;
