export const Final: MethodDecorator = (
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  target: Record<string, any>,
  key: string | symbol,
  descriptor: PropertyDescriptor
): PropertyDescriptor => ({
  ...descriptor,
  writable: false,
  configurable: false,
});
