import React from 'react';
import { CustomErrorCode } from 'constants/app.constants';
import { DefaultFallback } from 'components/ErrorBoundary/ErrorBoundary';

/* eslint-disable no-console */
export default mfImport => mfImport.catch(error => {
  console.log('%c MF Component failed to load:', 'background: red; color: #FFFFFF', error?.message);
  return {
    default: ({ children }) => children || <DefaultFallback code={CustomErrorCode.MF_LOAD} error={error} />,
  };
});
