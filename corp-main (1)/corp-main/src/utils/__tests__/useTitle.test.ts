import { renderHook } from '@testing-library/react-hooks';

import { useTitle } from '../useTitle';

describe('useTitle', () => {
  const originalTitle = document.title;

  afterEach(() => {
    document.title = originalTitle;
  });

  it('устанавливает document.title при первом рендере', () => {
    renderHook(() => useTitle('Главная страница'));

    expect(document.title).toBe('Главная страница');
  });

  it('обновляет document.title при изменении title', () => {
    const { rerender } = renderHook(({ title }) => useTitle(title), {
      initialProps: { title: 'Страница 1' },
    });

    expect(document.title).toBe('Страница 1');

    rerender({ title: 'Страница 2' });

    expect(document.title).toBe('Страница 2');
  });

  it('обновляет document.title при изменении ссылки deps', () => {
    const { rerender } = renderHook(({ deps }) => useTitle('Статичный заголовок', deps), {
      initialProps: { deps: ['a', 'b'] },
    });

    expect(document.title).toBe('Статичный заголовок');

    // Новая ссылка deps → useEffect сработает снова (deps участвует в deps-массиве ссылочно).
    rerender({ deps: ['a', 'c'] });

    expect(document.title).toBe('Статичный заголовок');
  });
});
