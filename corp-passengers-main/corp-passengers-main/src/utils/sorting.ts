export const stringSorter = (a: string, b: string): 0 | 1 | -1 => (
  a.localeCompare(b, undefined, { numeric: true }) as 0 | 1 | -1
);

export const defaultSorter = <T>(a: T, b: T): 0 | 1 | -1 => typeof a === 'string' && typeof b === 'string' ? stringSorter(a, b) : a === b ? 0 : a < b ? -1 : 1;

type Sorter<T> = (a: T, b: T) => 0 | 1 | -1;

export const compareBy: <T, K extends keyof T>(p: K, sorter?: Sorter<T[K]>) => Sorter<T> = (
  p,
  sorter = defaultSorter
) => (a, b) => sorter(a[p], b[p]);
