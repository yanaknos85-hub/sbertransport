export const getJoinedArrayAsString = (array: (string | number)[] | undefined, sep = ';'): string | undefined => {
  if (Array.isArray(array) && array.length) {
    return array.join(sep);
  }

  return undefined;
};
