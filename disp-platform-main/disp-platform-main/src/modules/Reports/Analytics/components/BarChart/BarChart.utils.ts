export const calculateRangeWidth = ({
  maxValue,
  minRangeWidth,
  rangeSign,
}: {
  maxValue: number;
  minRangeWidth: number;
  rangeSign: string;
}) => {
  const NUMBER_WIDTH = 10;
  const SIGN_WIDTH = 6;
  const PADDING_RIGHT = 6;

  const rangeContainerSize
    = `${maxValue}`.length * NUMBER_WIDTH // длина значения
    + rangeSign.length * SIGN_WIDTH // длина знака
    + SIGN_WIDTH // пробел между значением и знаком
    + PADDING_RIGHT; // отступ справа
  const result = Math.max(rangeContainerSize, minRangeWidth);

  return result;
};
