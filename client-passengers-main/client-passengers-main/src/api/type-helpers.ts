/* eslint-disable @typescript-eslint/no-unused-vars */
/* eslint-disable @typescript-eslint/no-explicit-any */
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

// Можно будет использовать после TS4.1

// type ButLast<T> = T extends [...(infer TT), infer _] ? (TT extends [] ? never : TT) : never;

// export type Prefixes<T extends Array<any>> = T extends [...(infer P), infer L]
//   ? P extends []
//     ? L extends []
//       ? never
//       : [L]
//     : T | Prefixes<P>
//   : never;
