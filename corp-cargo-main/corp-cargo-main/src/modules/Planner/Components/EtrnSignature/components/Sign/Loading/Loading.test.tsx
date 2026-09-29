import React from 'react';
import { render, screen } from '@testing-library/react';
import Loading from './Loading';

// ========================
//    Моки
// ========================

jest.mock('i18n', () => ({
  useTranslation: () => ({
    t: {
      Etrn: {
        signModal: {
          loading: {
            init: 'Подготовка данных',
            signing: 'Подписание',
            warning: 'Не закрывайте вкладку и не обновляйте страницу',
          },
        },
      },
    },
  }),
}));

jest.mock('shared/components/SpinWrapped/SpinWrapped', () => ({
  SpinWrapped: () => <div data-testid="spin-wrapped">SPIN</div>,
}));

jest.mock('./styles.module.scss', () => ({
  container: {
    container: 'container',
    title: 'title',
  },
}));

// ========================
//  Тесты
// ========================

describe('Loading', () => {
  describe('с type="init"', () => {
    it('рендерит заголовок инициализации', () => {
      render(<Loading type="init" />);
      expect(screen.getByText('Подготовка данных')).toBeInTheDocument();
    });

    it('рендерит SpinWrapped', () => {
      render(<Loading type="init" />);
      expect(screen.getByTestId('spin-wrapped')).toBeInTheDocument();
    });

    it('рендерит предупреждение', () => {
      render(<Loading type="init" />);
      expect(screen.getByText('Не закрывайте вкладку и не обновляйте страницу')).toBeInTheDocument();
    });
  });

  describe('с type="signing"', () => {
    it('рендерит заголовок подписания', () => {
      render(<Loading type="signing" />);
      expect(screen.getByText('Подписание')).toBeInTheDocument();
    });

    it('рендерит SpinWrapped', () => {
      render(<Loading type="signing" />);
      expect(screen.getByTestId('spin-wrapped')).toBeInTheDocument();
    });

    it('рендерит предупреждение', () => {
      render(<Loading type="signing" />);
      expect(screen.getByText('Не закрывайте вкладку и не обновляйте страницу')).toBeInTheDocument();
    });
  });

  describe('структура', () => {
    it('рендерит заголовок', () => {
      const { container } = render(<Loading type="init" />);
      const spans = container.querySelectorAll('span');
      // 1-й span — заголовок, 2-й span — warning
      expect(spans).toHaveLength(2);
      expect(spans[0]?.textContent).toBe('Подготовка данных');
    });

    it('рендерит корневой контейнер с 3 детьми (title, spin, warning)', () => {
      const { container } = render(<Loading type="signing" />);
      const root = container.firstChild as HTMLElement | null;
      expect(root).not.toBeNull();
      expect(root?.children).toHaveLength(3);
    });
  });
});
