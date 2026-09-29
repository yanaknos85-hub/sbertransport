import * as R from 'ramda';

/**
 * Создает объект-индекс по id из массива объектов
 * @template T - тип объекта с полем id
 * @param xs - массив объектов с полем id
 * @returns объект, где ключи - это id элементов, значения - сами элементы
 */
const indexById: <T extends { id: string }>(xs: T[]) => Record<string, T> = R.indexBy(R.prop('id'));

export default indexById;
