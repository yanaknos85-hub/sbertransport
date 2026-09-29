import { isTestStand } from '../isTestStand';

const setOrigin = (origin: string) => {
  Object.defineProperty(window, 'location', {
    value: { origin },
    writable: true,
    configurable: true,
  });
};

describe('isTestStand', () => {
  const originalLocation = window.location;

  afterEach(() => {
    Object.defineProperty(window, 'location', {
      value: originalLocation,
      writable: true,
      configurable: true,
    });
  });

  it('возвращает true для origin, содержащего ".platform."', () => {
    setOrigin('https://dev.platform.sberbank.ru');
    expect(isTestStand()).toBe(true);
  });

  it('возвращает true для origin, содержащего ".taxi."', () => {
    setOrigin('https://dev.taxi.sberbank.ru');
    expect(isTestStand()).toBe(true);
  });

  it('возвращает true для origin, содержащего ".cargo."', () => {
    setOrigin('https://dev.cargo.sberbank.ru');
    expect(isTestStand()).toBe(true);
  });

  it('возвращает true для origin, содержащего ".fleet."', () => {
    setOrigin('https://dev.fleet.sberbank.ru');
    expect(isTestStand()).toBe(true);
  });

  it('возвращает true для origin, содержащего "localhost"', () => {
    setOrigin('http://localhost:3000');
    expect(isTestStand()).toBe(true);
  });

  it('возвращает false для пром-стенда без тестовых префиксов', () => {
    setOrigin('https://corp.sberbank.ru');
    expect(isTestStand()).toBe(false);
  });
});
