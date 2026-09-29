/* eslint-disable no-console */
export default (mfImport: () => Promise<unknown>) => {
  const data = {
    data: {},
  };

  mfImport()
    .then(r => {
      data.data = { ...data.data, ...(r || {}) };
      return data;
    })
    .catch(error => {
      console.log('%c MF Data failed to load:', 'background: red; color: #FFFFFF', error?.message);
    });

  return data;
};

