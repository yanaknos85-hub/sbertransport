import React from 'react';
import preval from 'preval.macro';

export const BuildInfo = (): JSX.Element => {
  const dateTimeStamp = preval`module.exports = new Date().toLocaleString();`;
  return (
    <small style={{ color: '#706a6a' }}>
      Build:
      {dateTimeStamp}
    </small>
  );
};
