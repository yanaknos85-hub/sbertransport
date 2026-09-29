export const createTransportLabel = (
  brand: string | undefined,
  model: string | undefined,
  stateNumber: string | undefined
) => {
  return `${brand ?? ''} ${model ?? ''} ${stateNumber ?? ''}`.trim();
};
