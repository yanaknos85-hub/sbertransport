import React, { FC, useRef } from 'react';

import { memo } from 'react';
import styled from 'styled-components';
import { createPortal } from 'react-dom';
import { useOnClickOutside } from 'shared/hooks/useOnClickOutside';
import { ReactComponent as CloseIcon } from 'shared/icons/hamburger-close.svg';

const Root = styled.div<{ topOffset: number }>`
  padding: 24px;

  position: absolute;
  border-radius: 24px;
  background: #ffffff;
  box-shadow: 0 0 12px 6px rgb(0 0 0 / 25%);
  z-index: 1000;

  width: calc(100% - 24px);

  top: ${({ topOffset }) => `${topOffset}px`};
`;

const ButtonIcon = styled.button`
  position: absolute;
  right: 2px;
  top: 2px;

  background: none;
  color: inherit;
  padding: 0;
  outline: inherit;

  width: 42px;
  height: 42px;
  display: flex;

  align-items: center;
  justify-content: center;

  border: none;
`;

const CloseIconStyled = styled(CloseIcon)`
  width: 22px;
  height: 22px;
  color: #00000080;
`;

interface Props { children: React.ReactNode; topOffset: number; onClose: () => void }

export const Popup: FC<Props> = memo(({
  children, topOffset, onClose,
}) => {
  const ref = useRef<HTMLDivElement>(null);

  useOnClickOutside(ref, onClose);

  return (
    <>
      {createPortal(
        <Root ref={ref} topOffset={topOffset}>
          <ButtonIcon onClick={onClose}>
            <CloseIconStyled />
          </ButtonIcon>
          {children}
        </Root>, document.body)}
    </>
  );
});
