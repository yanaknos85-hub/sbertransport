export const Final: MethodDecorator = (
  target: Record<string, any>,
  key: string | symbol,
  descriptor: PropertyDescriptor
): PropertyDescriptor => ({
  ...descriptor,
  writable: false,
  configurable: false,
});
