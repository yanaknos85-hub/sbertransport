/* eslint-disable no-restricted-properties */
export const formatBytes = (bytes = 0, decimals = 2) => {
  if (!+bytes) {
    return '0 байт';
  }
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['байт', 'кб', 'мб', 'гб', 'тб'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
};
