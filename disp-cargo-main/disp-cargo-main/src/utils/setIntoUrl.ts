// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const setIntoUrl = (urlParams: URLSearchParams, obj: Record<string, any>, key: string) => {
  if (Array.isArray(obj[key])) {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    obj[key].forEach((val: any) => {
      urlParams.append(`${key}[]`, String(val));
    });
  } else if (obj[key] || obj[key] === 0 || obj[key] === false) {
    urlParams.append(key, String(obj[key]));
  }
};
