export const isEmptyFilter = (obj: Record<string, unknown> | undefined): boolean => {
  if (!obj) return true;
  return Object.values(obj).every(val => val === undefined
    || val === null
    || (Array.isArray(val) && val.length === 0)
    || (typeof val === 'string' && val.trim() === '')
  );
};
