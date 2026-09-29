export const logFatalError = (error: string) => (
  // eslint-disable-next-line no-console
  console.log(`%c MF fatal error! `, 'color: white; background: red; font-size: 14px', error)
);
