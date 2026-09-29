import React, { FC, ReactNode } from 'react';

/**
 * Параметры для создания вызываемого контекста
 */
interface ContextOptions {
  /**
   * Название контекста для отладки
   */
  name?: string;
}

/**
 * Создает вызываемый React контекст с генерируемым значением
 * @param value функция, возвращающая значение контекста
 * @param options параметры контекста (опционально name для отладки)
 * @returns кортеж из хука useContext и Provider компонента
 * @example
 * const [useAuth, AuthProvider] = createCallableCtx(() => ({ user: 'test' }), { name: 'AuthContext' });
 *
 * function App() {
 *   return (
 *     <AuthProvider>
 *       <MyComponent />
 *     </AuthProvider>
 *   );
 * }
 *
 * function MyComponent() {
 *   const { user } = useAuth();
 *   return <div>{user}</div>;
 * }
 */
export function createCallableCtx<R>(value: () => R, { name }: ContextOptions): readonly [() => R, FC<ReactNode>] {
  const Ctx = React.createContext<R>({ ctxDefault: true } as R);

  const useContext = () => {
    const context = React.useContext(Ctx);

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    if ((context as any).ctxDefault) {
      throw new Error(`useContext must be inside a ${name} Provider with a value`);
    }
    return context;
  };

  const Provider: FC = ({ children }) => <Ctx.Provider value={value()}>{children}</Ctx.Provider>;

  if (name) {
    Provider.displayName = name;
  }

  return [useContext, Provider] as const;
}
