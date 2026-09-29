import * as React from 'react';

import { memo } from 'react';
import { createPortal } from 'react-dom';
import styled from 'styled-components';

const Root = styled.div<{ topOffset: number }>`
  padding-top: 12px;
  position: absolute;
  border-radius: 0 0 24px 24px;
  background: #f7f8fa;
  box-shadow: 0px 6px 12px rgb(0 0 0 / 25%);
  z-index: 1000;

  width: 100%;
  top: ${({ topOffset }) => `${topOffset}px`};
`;

interface Props { children: React.ReactNode; onClose: () => void; topOffset: number }

export const MenuMobile = memo(({ children, topOffset }: Props) => {
  return <>{createPortal(<Root topOffset={topOffset}>{children}</Root>, document.body)}</>;
});
