/* eslint-disable @typescript-eslint/explicit-function-return-type */
/* eslint-disable react/prefer-stateless-function */
import React from 'react';
import ErrorBoundary from 'shared/components/ErrorBoundary';

const withErrorBoundary = <P extends Record<string, any>>(
  WrappedComponent: React.ComponentType<P>
): React.ForwardRefExoticComponent<React.PropsWithoutRef<P> & React.RefAttributes<unknown>> => {
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
