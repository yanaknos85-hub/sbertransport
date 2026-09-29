import * as t from 'io-ts';
import { PathReporter } from 'io-ts/lib/PathReporter';

import { ioTypeFromEnum } from '../ioTypeFromEnum';

enum Color {
  RED = 'RED',
  GREEN = 'GREEN',
  BLUE = 'BLUE',
}

const ColorC = ioTypeFromEnum<Color>('Color', Color);

describe('ioTypeFromEnum', () => {
  it('создаёт тип с указанным именем', () => {
    expect(ColorC.name).toBe('Color');
  });

  it('is возвращает true для значения из перечисления', () => {
    expect(ColorC.is('RED')).toBe(true);
    expect(ColorC.is('GREEN')).toBe(true);
    expect(ColorC.is('BLUE')).toBe(true);
  });

  it('is возвращает false для значения не из перечисления', () => {
    expect(ColorC.is('YELLOW')).toBe(false);
    expect(ColorC.is('red')).toBe(false);
  });

  it('is возвращает false для типов, отличных от значений перечисления', () => {
    expect(ColorC.is(undefined)).toBe(false);
    expect(ColorC.is(null)).toBe(false);
    expect(ColorC.is({})).toBe(false);
    expect(ColorC.is(0)).toBe(false);
  });

  it('decode возвращает Right для валидного значения', () => {
    const result = ColorC.decode('RED');

    expect(result._tag).toBe('Right');
    expect((result as t.Right<Color>).right).toBe('RED');
  });

  it('decode возвращает Left для невалидного значения', () => {
    const result = ColorC.decode('YELLOW');

    expect(result._tag).toBe('Left');
    const errors = PathReporter.report(result as t.Left<unknown>);
    expect(errors.length).toBeGreaterThan(0);
  });

  it('encode возвращает значение без изменений', () => {
    expect(ColorC.encode('RED')).toBe('RED');
    expect(ColorC.encode('GREEN')).toBe('GREEN');
  });
});
