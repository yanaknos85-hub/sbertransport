import React from 'react';
import { toRubles } from 'utils';

export const rub = (value: number) => `${toRubles(value, true).toFixed(2)} &#8381`;

export const RubleSymbolComponent = ({ value }) => (
  <span dangerouslySetInnerHTML={{ __html: rub(value) }} />
);
