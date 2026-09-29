/* eslint-disable @typescript-eslint/no-unused-vars */
type ButLast<T extends any[]> = T extends []
  ? never
  : T extends [infer _]
    ? []
    : T extends [infer A, infer _]
      ? [A]
      : T extends [infer A, infer B, infer _]
        ? [A, B]
        : T extends [infer A, infer B, infer C, infer _]
          ? [A, B, C]
          : T extends [infer A, infer B, infer C, infer D, infer _]
            ? [A, B, C, D]
            : T extends [infer A, infer B, infer C, infer D, infer E, infer _]
              ? [A, B, C, D, E]
              : never;

export type Prefixes<T extends any[]> = {
  _: [];
  rec: T | Prefixes<ButLast<T>>;
}[T extends [] ? '_' : 'rec'];
