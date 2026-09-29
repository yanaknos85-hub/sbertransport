import * as R from 'ramda';
import * as React from 'react';

import { Toolbar, ToolbarElement, ToolbarProvider } from '.';

export default {
  title: 'Toolbar',
  component: Toolbar,
};

const CounterButton: React.FC = () => {
  const [count, set] = React.useState(0);

  const increment = React.useCallback(() => set(x => x + 1), [set]);

  return (
    <button type="button" onClick={increment}>
      Pressed
      {' '}
      {count}
      {' '}
      times
    </button>
  );
};

export const ToolbarStory: React.FC = () => {
  const [showAdditional, setShowAdditional] = React.useState(false);

  const toggle = React.useCallback(() => {
    setShowAdditional(R.not);
  }, [setShowAdditional]);

  return (
    <div>
      <ToolbarProvider>
        <Toolbar />

        <ToolbarElement>
          <CounterButton />
        </ToolbarElement>
        <ToolbarElement>
          <button type="button" onClick={toggle}>
            TEST
          </button>
        </ToolbarElement>
        {showAdditional ? <ToolbarElement>Additional element</ToolbarElement> : null}
      </ToolbarProvider>
    </div>
  );
};
