/* eslint-disable @stylistic/max-statements-per-line */
export const declOfNum = (n: number) => {
  const array = ['пассажир', 'пассажира', 'пассажиров'];
  n = Math.abs(n) % 100;
  const n1 = n % 10;
  if (n > 10 && n < 20) { return `${n} ${array[2]}`; }
  if (n1 > 1 && n1 < 5) { return `${n} ${array[1]}`; }
  if (n1 == 1) { return `${n} ${array[0]}`; }
  return `${n} ${array[2]}`;
};
