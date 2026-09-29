import * as t from 'io-ts';
import { either as Either } from 'fp-ts';

/**
 * Создает io-ts тип, который оборачивает декодированное значение в класс
 * @param typ - Базовый io-ts тип для декодирования
 * @param Cls - Конструктор класса, в который будет обернуто значение
 * @returns io-ts тип, который возвращает экземпляр класса
 * @example
 * class User {
 *   constructor(public name: string) {}
 * }
 *
 * const UserCodec = wrapped(t.type({ name: t.string }), User);
 * // decode({ name: 'John' }) -> User { name: 'John' }
 */
export function wrapped<T, C>(typ: t.Type<T>, Cls: new (x: T) => C): t.Type<C, C, unknown> {
  return new t.Type<C, C, unknown>(
    Cls.name,
    (x): x is C => x instanceof Cls,
    (u, _c) => Either.map<T, C>(x => new Cls(x))(typ.decode(u)),
    c => ({ ...c })
  );
}
