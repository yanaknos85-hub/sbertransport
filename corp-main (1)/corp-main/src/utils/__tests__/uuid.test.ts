import uuid from '../uuid';

const UUID_V4_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/;

describe('uuid', () => {
  it('возвращает строку длиной 36 символов', () => {
    const result = uuid();

    expect(result).toHaveLength(36);
  });

  it('соответствует формату UUID v4', () => {
    const result = uuid();

    expect(result).toMatch(UUID_V4_REGEX);
  });

  it('третий сегмент начинается с "4" (версия UUID v4)', () => {
    const result = uuid();

    expect(result.split('-')[2][0]).toBe('4');
  });

  it('четвёртый сегмент начинается с одного из 8, 9, a, b (variant)', () => {
    const result = uuid();
    const fourthSegmentFirstChar = result.split('-')[3][0];

    expect(['8', '9', 'a', 'b']).toContain(fourthSegmentFirstChar);
  });

  it('возвращает уникальные значения при множественных вызовах', () => {
    const ids = new Set<string>();

    for (let i = 0; i < 100; i++) {
      ids.add(uuid());
    }

    expect(ids.size).toBe(100);
  });

  it('все сегменты разделены дефисами и имеют правильную длину', () => {
    const result = uuid();
    const segments = result.split('-');

    expect(segments).toHaveLength(5);
    expect(segments[0]).toHaveLength(8);
    expect(segments[1]).toHaveLength(4);
    expect(segments[2]).toHaveLength(4);
    expect(segments[3]).toHaveLength(4);
    expect(segments[4]).toHaveLength(12);
  });
});
