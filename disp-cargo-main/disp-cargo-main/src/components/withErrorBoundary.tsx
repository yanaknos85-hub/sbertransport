import React from 'react';

import ErrorBoundary from 'components/ErrorBoundary';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const withErrorBoundary = <P extends Record<string, any>>(WrappedComponent: React.ComponentType<P>) => {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  class Wrapper extends React.Component<P & { forwardedRef: React.Ref<any> }> {
    render() {
      const { forwardedRef } = this.props;
      return (
        <ErrorBoundary>
          <WrappedComponent ref={forwardedRef} {...this.props} />
        </ErrorBoundary>
      );
    }
  }

  return React.forwardRef((props: P, ref) => <Wrapper {...props} forwardedRef={ref} />);
};

export default withErrorBoundary;
