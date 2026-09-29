import React, { FC, ReactNode } from 'react';

interface ContextOptions {
  name?: string;
}

export function createCallableCtx<R>(value: () => R, { name }: ContextOptions): readonly [() => R, FC<ReactNode>] {
  const Ctx = React.createContext<R>({ ctxDefault: true } as R);

  const useContext = () => {
    const context = React.useContext(Ctx);

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    if ((context as any).ctxDefault) {
      throw new Error('useContext must be inside a Provider with a value');
    }
    return context;
  };

  const Provider: FC = ({ children }) => <Ctx.Provider value={value()}>{children}</Ctx.Provider>;

  if (name) {
    Provider.displayName = name;
  }

  return [useContext, Provider] as const;
}
