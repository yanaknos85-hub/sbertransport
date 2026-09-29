import { either as Either } from 'fp-ts';
import * as t from 'io-ts';

export function wrapped<T, C>(typ: t.Type<T>, Cls: new (x: T) => C): t.Type<C, C, unknown> {
  return new t.Type<C, C, unknown>(
    Cls.name,
    (x): x is C => x instanceof Cls,
    u => Either.map<T, C>(x => new Cls(x))(typ.decode(u)),
    c => ({ ...c })
  );
}
