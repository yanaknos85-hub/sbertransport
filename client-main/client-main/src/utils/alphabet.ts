function genCharArray(charA: string, charZ: string) {
  const a = [];
  let i = charA.charCodeAt(0);
  const j = charZ.charCodeAt(0);
  // eslint-disable-next-line no-plusplus
  for (; i <= j; ++i) {
    a.push(String.fromCharCode(i));
  }
  return a;
}

export { genCharArray };
