import React from 'react';
import { render, screen, cleanup } from '@testing-library/react';
import { createCallableCtx } from '../createCallableContext';

// Clean up after each test to avoid context leakage
afterEach(() => {
  cleanup();
});

describe('createCallableCtx', () => {
  describe('Basic functionality', () => {
    it('should return correct value when used inside Provider', () => {
      const value = { user: 'test-user', id: 123 };
      const [useContext, Provider] = createCallableCtx(() => value, { name: 'TestContext' });

      function TestComponent() {
        const ctx = useContext();
        return <div data-testid="result">{ctx.user}</div>;
      }

      render(
        <Provider>
          <TestComponent />
        </Provider>
      );

      expect(screen.getByTestId('result')).toHaveTextContent('test-user');
    });

    it('should throw error when hook is used outside Provider', () => {
      const [useContext] = createCallableCtx(() => ({ user: 'test' }), { name: 'TestContext' });

      function TestComponent() {
        useContext();
        return <div>Test</div>;
      }

      expect(() => {
        render(<TestComponent />);
      }).toThrow('useContext must be inside a TestContext Provider with a value');
    });

    it('should accept custom value function', () => {
      const [useContext, Provider] = createCallableCtx(
        () => ({ timestamp: Date.now() }),
        { name: 'TimestampContext' }
      );

      function TestComponent() {
        const ctx = useContext();
        return <div data-testid="timestamp">{ctx.timestamp}</div>;
      }

      const timestamp = Date.now();
      render(
        <Provider>
          <TestComponent />
        </Provider>
      );

      const renderedTimestamp = parseInt(screen.getByTestId('timestamp').textContent || '0', 10);
      expect(renderedTimestamp).toBeGreaterThanOrEqual(timestamp);
    });
  });

  describe('Name option', () => {
    it('should apply name to Provider displayName', () => {
      const [, Provider] = createCallableCtx(() => ({}), { name: 'AuthContext' });

      expect(Provider.displayName).toBe('AuthContext');
    });

    it('should work without name option', () => {
      const [, Provider] = createCallableCtx(() => ({}), {});

      expect(Provider.displayName).toBeUndefined();
    });

    it('should use provided name in error message', () => {
      const [useContext] = createCallableCtx(() => ({}), { name: 'CustomNameContext' });

      function TestComponent() {
        useContext();
        return <div>Test</div>;
      }

      expect(() => {
        render(<TestComponent />);
      }).toThrow('useContext must be inside a CustomNameContext Provider with a value');
    });
  });

  describe('Multiple components', () => {
    it('should allow multiple components to use the same context', () => {
      const value = { count: 0 };
      const [useContext, Provider] = createCallableCtx(() => value, { name: 'CountContext' });

      function ComponentA() {
        const ctx = useContext();
        return <div data-testid="a">{ctx.count}</div>;
      }

      function ComponentB() {
        const ctx = useContext();
        return <div data-testid="b">{ctx.count}</div>;
      }

      render(
        <Provider>
          <ComponentA />
          <ComponentB />
        </Provider>
      );

      expect(screen.getByTestId('a')).toHaveTextContent('0');
      expect(screen.getByTestId('b')).toHaveTextContent('0');
    });

    it('should share same value across multiple components', () => {
      const [useContext, Provider] = createCallableCtx(
        () => ({ shared: 'value' }),
        { name: 'SharedContext' }
      );

      function ComponentA() {
        const ctx = useContext();
        return <div data-testid="a">{ctx.shared}</div>;
      }

      function ComponentB() {
        const ctx = useContext();
        return <div data-testid="b">{ctx.shared}</div>;
      }

      render(
        <Provider>
          <ComponentA />
          <ComponentB />
        </Provider>
      );

      expect(screen.getByTestId('a')).toHaveTextContent('value');
      expect(screen.getByTestId('b')).toHaveTextContent('value');
    });
  });

  describe('Context isolation', () => {
    it('should properly isolate values between different context instances', () => {
      const [useContext1, Provider1] = createCallableCtx(
        () => ({ context: 'first', value: 1 }),
        { name: 'FirstContext' }
      );

      const [useContext2, Provider2] = createCallableCtx(
        () => ({ context: 'second', value: 2 }),
        { name: 'SecondContext' }
      );

      function Component1() {
        const ctx = useContext1();
        return (
          <div data-testid="ctx1">
            {ctx.context}
            :
            {ctx.value}
          </div>
        );
      }

      function Component2() {
        const ctx = useContext2();
        return (
          <div data-testid="ctx2">
            {ctx.context}
            :
            {ctx.value}
          </div>
        );
      }

      render(
        <>
          <Provider1>
            <Component1 />
          </Provider1>
          <Provider2>
            <Component2 />
          </Provider2>
        </>
      );

      expect(screen.getByTestId('ctx1')).toHaveTextContent('first:1');
      expect(screen.getByTestId('ctx2')).toHaveTextContent('second:2');
    });
  });

  describe('Component rendering', () => {
    it('should render children correctly', () => {
      const [useContext, Provider] = createCallableCtx(() => ({}), { name: 'TestContext' });

      function TestComponent() {
        useContext();
        return <div data-testid="child">Child Content</div>;
      }

      render(
        <Provider>
          <TestComponent />
        </Provider>
      );

      expect(screen.getByTestId('child')).toHaveTextContent('Child Content');
    });

    it('should handle null children', () => {
      const [useContext, Provider] = createCallableCtx(() => ({}), { name: 'TestContext' });

      function TestComponent() {
        useContext();
        return null;
      }

      const { container } = render(
        <Provider>
          <TestComponent />
        </Provider>
      );

      expect(container.firstChild).toBeNull();
    });

    it('should handle React fragments as children', () => {
      const [useContext, Provider] = createCallableCtx(() => ({}), { name: 'TestContext' });

      function TestComponent() {
        useContext();
        return (
          <>
            <span data-testid="span1">First</span>
            <span data-testid="span2">Second</span>
          </>
        );
      }

      render(
        <Provider>
          <TestComponent />
        </Provider>
      );

      expect(screen.getByTestId('span1')).toHaveTextContent('First');
      expect(screen.getByTestId('span2')).toHaveTextContent('Second');
    });
  });

  describe('Value function behavior', () => {
    it('should call value function on each render of Provider', () => {
      let callCount = 0;
      const [, Provider] = createCallableCtx(
        () => {
          callCount++;
          return { count: callCount };
        },
        { name: 'CounterContext' }
      );

      const { rerender } = render(
        <Provider>
          <div>Test</div>
        </Provider>
      );

      expect(callCount).toBe(1);

      // Rerender the provider with new props (triggering re-render)
      rerender(
        <Provider>
          <div>Test 2</div>
        </Provider>
      );

      expect(callCount).toBe(2);
    });

    it('should handle primitive values', () => {
      const [useContext, Provider] = createCallableCtx(() => 'string-value', {
        name: 'StringContext',
      });

      function TestComponent() {
        const ctx = useContext();
        return <div data-testid="result">{ctx}</div>;
      }

      render(
        <Provider>
          <TestComponent />
        </Provider>
      );

      expect(screen.getByTestId('result')).toHaveTextContent('string-value');
    });

    it('should handle numeric values', () => {
      const [useContext, Provider] = createCallableCtx(() => 42, {
        name: 'NumberContext',
      });

      function TestComponent() {
        const ctx = useContext();
        return <div data-testid="result">{ctx}</div>;
      }

      render(
        <Provider>
          <TestComponent />
        </Provider>
      );

      expect(screen.getByTestId('result')).toHaveTextContent('42');
    });

    it('should handle boolean values', () => {
      const [useContext, Provider] = createCallableCtx(() => true, {
        name: 'BooleanContext',
      });

      function TestComponent() {
        const ctx = useContext();
        return <div data-testid="result">{String(ctx)}</div>;
      }

      render(
        <Provider>
          <TestComponent />
        </Provider>
      );

      expect(screen.getByTestId('result')).toHaveTextContent('true');
    });

    it('should handle array values', () => {
      const [useContext, Provider] = createCallableCtx(() => [1, 2, 3], {
        name: 'ArrayContext',
      });

      function TestComponent() {
        const ctx = useContext();
        return <div data-testid="result">{ctx.join(',')}</div>;
      }

      render(
        <Provider>
          <TestComponent />
        </Provider>
      );

      expect(screen.getByTestId('result')).toHaveTextContent('1,2,3');
    });

    it('should handle object values with nested properties', () => {
      const [useContext, Provider] = createCallableCtx(
        () => ({
          user: {
            name: 'John',
            age: 30,
          },
        }),
        { name: 'NestedContext' }
      );

      function TestComponent() {
        const ctx = useContext();
        return <div data-testid="result">{ctx.user.name}</div>;
      }

      render(
        <Provider>
          <TestComponent />
        </Provider>
      );

      expect(screen.getByTestId('result')).toHaveTextContent('John');
    });
  });
});
