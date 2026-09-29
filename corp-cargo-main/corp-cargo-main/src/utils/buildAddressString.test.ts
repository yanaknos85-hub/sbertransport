import { buildAddressString } from './buildAddressString';

describe('buildAddressString', () => {
  test('должен собрать полный адрес со всеми полями', () => {
    const waypoint = {
      street: 'Lenina',
      house: '123',
      city: 'Moscow',
      latitude: 55.75,
      longitude: 37.61,
    };
    expect(buildAddressString(waypoint)).toBe('Lenina, 123, Moscow');
  });

  test('должен работать только с улицей', () => {
    const waypoint = {
      street: 'Lenina',
      latitude: 55.75,
      longitude: 37.61,
    };
    expect(buildAddressString(waypoint)).toBe('Lenina,');
  });

  test('должен работать только с домом', () => {
    const waypoint = {
      house: '123',
      latitude: 55.75,
      longitude: 37.61,
    };
    expect(buildAddressString(waypoint)).toBe('123,');
  });

  test('должен работать только с городом', () => {
    const waypoint = {
      city: 'Moscow',
      latitude: 55.75,
      longitude: 37.61,
    };
    expect(buildAddressString(waypoint)).toBe('Moscow');
  });

  test('должен собрать адрес с улицей и домом', () => {
    const waypoint = {
      street: 'Lenina',
      house: '123',
      latitude: 55.75,
      longitude: 37.61,
    };
    expect(buildAddressString(waypoint)).toBe('Lenina, 123,');
  });

  test('должен собрать адрес с улицей и городом', () => {
    const waypoint = {
      street: 'Lenina',
      city: 'Moscow',
      latitude: 55.75,
      longitude: 37.61,
    };
    expect(buildAddressString(waypoint)).toBe('Lenina, Moscow');
  });

  test('должен собрать адрес с домом и городом', () => {
    const waypoint = {
      house: '123',
      city: 'Moscow',
      latitude: 55.75,
      longitude: 37.61,
    };
    expect(buildAddressString(waypoint)).toBe('123, Moscow');
  });

  test('должен возвращать пустую строку для минимального waypoint', () => {
    const waypoint = {
      latitude: 55.75,
      longitude: 37.61,
    };
    expect(buildAddressString(waypoint)).toBe('');
  });

  test('должен игнорировать пустые строки', () => {
    const waypoint = {
      street: '',
      house: '',
      city: '',
      latitude: 55.75,
      longitude: 37.61,
    };
    expect(buildAddressString(waypoint)).toBe('');
  });

  test('должен игнорировать null и undefined значения', () => {
    const waypoint = {
      street: undefined as unknown as string,
      house: null as unknown as string,
      city: '',
      latitude: 55.75,
      longitude: 37.61,
    };
    expect(buildAddressString(waypoint)).toBe('');
  });

  test('должен работать с пробелами в значениях', () => {
    const waypoint = {
      street: 'Lenina Street',
      house: '123 A',
      city: 'Moscow City',
      latitude: 55.75,
      longitude: 37.61,
    };
    expect(buildAddressString(waypoint)).toBe('Lenina Street, 123 A, Moscow City');
  });
});
