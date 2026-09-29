// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const searchSymbol = (input: string, value?: any): boolean => (
  value?.label.toLowerCase().indexOf(input.toLowerCase()) >= 0
);
