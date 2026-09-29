import { UUID } from 'utils/io-ts';

import { removeIdFromUrl } from '../removeIdFromUrl';

const uuid = (v: string): UUID => v as UUID;

describe('removeIdFromUrl', () => {
  it('удаляет UUID в конце URL', () => {
    expect(removeIdFromUrl(uuid('/api/v1/items/3fa85f64-5717-4562-b3fc-2c963f66afa6')))
      .toBe('/api/v1/items');
  });

  it('удаляет UUID в нижнем регистре', () => {
    expect(removeIdFromUrl(uuid('/path/3fa85f64-5717-4562-b3fc-2c963f66afa6')))
      .toBe('/path');
  });

  it('удаляет UUID в верхнем регистре', () => {
    expect(removeIdFromUrl(uuid('/path/3FA85F64-5717-4562-B3FC-2C963F66AFA6')))
      .toBe('/path');
  });

  it('оставляет URL без изменений, если UUID отсутствует', () => {
    expect(removeIdFromUrl(uuid('/api/v1/items')))
      .toBe('/api/v1/items');
  });

  it('оставляет URL без изменений, если в конце меньше 36 hex-символов', () => {
    expect(removeIdFromUrl(uuid('/api/3fa85f64-5717')))
      .toBe('/api/3fa85f64-5717');
  });

  it('оставляет URL без изменений, если в конце больше 36 hex-символов', () => {
    expect(removeIdFromUrl(uuid('/api/3fa85f64-5717-4562-b3fc-2c963f66afa6-extra')))
      .toBe('/api/3fa85f64-5717-4562-b3fc-2c963f66afa6-extra');
  });

  it('оставляет URL без изменений, если UUID не в конце строки', () => {
    expect(removeIdFromUrl(uuid('/3fa85f64-5717-4562-b3fc-2c963f66afa6/items')))
      .toBe('/3fa85f64-5717-4562-b3fc-2c963f66afa6/items');
  });

  it('оставляет URL без изменений, если в конце не-hex символы', () => {
    expect(removeIdFromUrl(uuid('/api/not-a-uuid-but-36-chars-long-stringg')))
      .toBe('/api/not-a-uuid-but-36-chars-long-stringg');
  });

  it('возвращает пустую строку без изменений', () => {
    expect(removeIdFromUrl(uuid(''))).toBe('');
  });
});
