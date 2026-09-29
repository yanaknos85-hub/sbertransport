// Original code here: https://gist.github.com/gordonbrander/2230317
const uniqId = (prefix = '', postfix = ''): string => {
  const a = new Uint32Array(3);
  window.crypto.getRandomValues(a);
  const id = (
    performance.now().toString(36)
    + Array.from(a)
      .map(A => A.toString(36))
      .join('')
  ).replace(/\./g, '');
  return `${prefix}${id}${postfix}`;
};

export default uniqId;
