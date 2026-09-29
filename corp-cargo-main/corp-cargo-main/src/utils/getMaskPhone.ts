export const getMaskPhone = (value: string | undefined): string | undefined => {
  if (value) {
    return value.replace(/\(|\)|\s|-|_/g, '');
  }
  return undefined;
};
