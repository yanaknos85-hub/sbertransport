/* eslint-disable camelcase */
/* eslint-disable guard-for-in */
/* eslint-disable no-restricted-syntax */
/* eslint-disable @typescript-eslint/no-explicit-any */
type TObj = Record<string, any>;

export const checkingValueObjectNotNull = (obj: TObj): boolean => {
  for (const key in obj) {
    const val = obj[key];

    if (val instanceof Object) {
      const any_no_null = checkingValueObjectNotNull(val);
      if (any_no_null) return true;
    } else if (val !== null) {
      return true;
    }
  }

  return false;
};
