export interface PromiseConstructorParams<T> {
  resolve: (value?: T) => void;
  reject: (reason?: unknown) => void;
}
