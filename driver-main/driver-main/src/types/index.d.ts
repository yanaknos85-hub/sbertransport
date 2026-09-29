type Dictionary<T> = Record<string, T>;

type ValueOf<T> = T[keyof T];

type PartialBy<T, K extends keyof T> = Omit<T, K> & Partial<Pick<T, K>>;

type Override<T, O> = Omit<T, keyof O> & O;

type FormItem<C extends React.ComponentType<{ value: any; onChange: any } & any>> = React.ComponentType<
  Omit<React.ComponentProps<C>, 'value' | 'onChange'>
>;

interface WindowEventMap {
  'privacyPolicyAgreed': Event;
}
