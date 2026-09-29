import React, { FC, Suspense, SuspenseProps } from 'react';
import { SpinWrapped } from 'components/SpinWrapped/SpinWrapped';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const withSuspense = <T extends Record<string, any>>(
  WrappedComponent: FC<T>,
  suspenseProps?: Partial<SuspenseProps>
) => {
  return (props: T) => (
    <Suspense fallback={<SpinWrapped />} {...suspenseProps}>
      <WrappedComponent {...props} />
    </Suspense>
  );
};

export default withSuspense;
