import React from 'react';
import { APP_NAME } from 'constants/constants.env';
import { CustomErrorCode } from 'constants/constants.app';
import { DefaultFallback } from 'shared/components/ErrorBoundary/ErrorBoundary';

/* eslint-disable no-console */
export default mfImport => mfImport.catch(error => {
  console.log(`%c MF <${APP_NAME}> Component failed to load:`, 'background: red; color: #FFFFFF', error);
  return {
    default: ({ children }) => children || <DefaultFallback code={CustomErrorCode.MF_LOAD} error={error} />,
  };
});
