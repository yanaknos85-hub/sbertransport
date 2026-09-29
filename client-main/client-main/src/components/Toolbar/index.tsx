import * as React from 'react';
import { reject, append, whereEq } from 'ramda';

import uuid from 'utils/uuid';
import { UUID } from 'utils/io-ts';
import styled from 'styled-components';

interface ToolbarElement {
  id: UUID;
  children: React.ReactNode;
}

type ToolbarContext = [ToolbarElement[], React.Dispatch<React.SetStateAction<ToolbarElement[]>>];

const ToolbarContext = React.createContext<ToolbarContext | null>(null);

export const useToolbar = (): ToolbarContext => {
  const ctx = React.useContext(ToolbarContext);

  if (!ctx) {
    throw new Error('Toolbar context requested but not provided');
  }

  return ctx;
};

const useToolbarElement = () => {
  const [, update] = useToolbar();

  return React.useMemo(() => {
    const id = uuid();

    return {
      register: (children: React.ReactNode) => update(append({ id, children })),
      update: (children: React.ReactNode) => update(xs => xs.map(e => (e.id === id ? { id, children } : e))),
      unregister: () => update(reject(whereEq({ id }))),
    };
  }, [update]);
};

export const ToolbarProvider: React.FC = ({ children }) => (
  <ToolbarContext.Provider value={React.useState<ToolbarElement[]>([])}>{children}</ToolbarContext.Provider>
);

export const ToolbarElement: React.FC = ({ children }) => {
  const {
    register, update, unregister,
  } = useToolbarElement();

  const [registered, setRegistered] = React.useState(false);

  React.useEffect(() => {
    if (registered) {
      update(children);
    } else {
      register(children);
      setRegistered(true);
    }
  }, [register, update, children, registered]);

  React.useEffect(() => unregister, [unregister]);

  return null;
};

const StyledToolbar = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 8px;
`;

const Toolbar: React.FC<{ className?: string }> = ({ className }) => {
  const [elements] = useToolbar();

  return (
    <StyledToolbar className={className}>
      {elements.map(({ id, children }) => (
        <React.Fragment key={id}>{children}</React.Fragment>
      ))}
    </StyledToolbar>
  );
};

const TB = styled(Toolbar)``;

export { TB as Toolbar };
