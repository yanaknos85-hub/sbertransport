export const letterEndingFactory = (texts: [string, string, string]): ((value?: number) => string) => {
  function _common(value?: number): string {
    if (value === undefined) {
      return '';
    }

    const cases = [2, 0, 1, 1, 1, 2];

    if (value % 100 > 4 && value % 100 < 20) {
      return texts[2];
    }

    if (value % 10 < 5) {
      return texts[cases[value % 10]];
    }

    return texts[cases[5]];
  }

  return (value?: number): string => _common(value);
};

export const letterEndingMinutes = (value?: number): string => letterEndingFactory(['минута', 'минуты', 'минут'])(value);

export const letterEndingRubles = (value?: number): string => letterEndingFactory(['рубль', 'рубля', 'рублей'])(value);
