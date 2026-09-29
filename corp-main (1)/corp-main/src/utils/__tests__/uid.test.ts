import uniqId from '../uid';

// jsdom не предоставляет window.crypto по умолчанию.
// В реальной среде функция полагается на WebCrypto.
beforeAll(() => {
  Object.defineProperty(window, 'crypto', {
    value: {
      getRandomValues: (array: Uint32Array) => {
        for (let i = 0; i < array.length; i++) {
          array[i] = Math.floor(Math.random() * 0xffffffff);
        }
        return array;
      },
    },
    configurable: true,
  });
});

describe('uniqId', () => {
  it('возвращает непустую строку', () => {
    const result = uniqId();

    expect(typeof result).toBe('string');
    expect(result.length).toBeGreaterThan(0);
  });

  it('возвращает уникальные значения при множественных вызовах', () => {
    const ids = new Set<string>();

    for (let i = 0; i < 100; i++) {
      ids.add(uniqId());
    }

    expect(ids.size).toBe(100);
  });

  it('применяет prefix в начале строки', () => {
    const result = uniqId('item-');

    expect(result.startsWith('item-')).toBe(true);
  });

  it('применяет postfix в конце строки', () => {
    const result = uniqId('', '-end');

    expect(result.endsWith('-end')).toBe(true);
  });

  it('применяет и prefix, и postfix одновременно', () => {
    const result = uniqId('pre-', '-post');

    expect(result.startsWith('pre-')).toBe(true);
    expect(result.endsWith('-post')).toBe(true);
  });

  it('не содержит точек в результирующей строке', () => {
    const result = uniqId();

    expect(result.includes('.')).toBe(false);
  });

  it('работает без аргументов (использует пустые prefix и postfix)', () => {
    const result = uniqId();

    expect(result.length).toBeGreaterThan(0);
  });
});
