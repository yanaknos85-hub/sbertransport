import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { Filters } from './Filters';

// ========================
//        Моки
// ========================

jest.mock('shared/form/FormField/FormField', () => {
  const MockFormField = (props: { name?: string; label?: string; type?: string; params?: Record<string, unknown> }) => {
    const { params, label, type, name } = props;
    // Сохраняем все props в DOM-атрибуты для верификации
    const style = (params?.style as Record<string, unknown>) ?? {};
    const suffix = params?.suffix as React.ReactNode;
    return (
      <div
        data-testid="form-field"
        data-name={name ?? ''}
        data-label={label ?? ''}
        data-type={type ?? ''}
        data-placeholder={(params?.placeholder as string) ?? ''}
        data-bg={(style.backgroundColor as string) ?? ''}
        data-font={(style.fontFamily as string) ?? ''}
      >
        {suffix && <div data-testid="filter-suffix">{suffix}</div>}
      </div>
    );
  };
  return { __esModule: true, default: MockFormField };
});

jest.mock('shared/styles/styles', () => ({
  colors: {
    bgDark: '#f0f0f0',
    primary: '#10bf6a',
  },
  fontFamily: {
    SBSansTextRegular: 'SBSansTextRegular',
  },
}));

jest.mock('./Filters.styles', () => ({
  __esModule: true,
  FormWrapper: ({ children }: { children: React.ReactNode }) => (
    <form data-testid="form-wrapper">{children}</form>
  ),
  Button: ({ children, onClick }: { children: React.ReactNode; onClick?: () => void }) => (
    <button data-testid="settings-button" type="button" onClick={onClick}>
      {children}
    </button>
  ),
}));

// ========================
//       Тесты
// ========================

describe('Filters', () => {
  describe('рендеринг', () => {
    it('должен рендерить FormWrapper (form)', () => {
      render(<Filters />);
      expect(screen.getByTestId('form-wrapper')).toBeInTheDocument();
    });

    it('должен рендерить FormField', () => {
      render(<Filters />);
      expect(screen.getByTestId('form-field')).toBeInTheDocument();
    });

    it('должен передавать name="find" в FormField', () => {
      render(<Filters />);
      expect(screen.getByTestId('form-field').getAttribute('data-name')).toBe('find');
    });

    it('должен передавать пустой label в FormField', () => {
      render(<Filters />);
      expect(screen.getByTestId('form-field').getAttribute('data-label')).toBe('');
    });

    it('должен передавать type="input" в FormField', () => {
      render(<Filters />);
      expect(screen.getByTestId('form-field').getAttribute('data-type')).toBe('input');
    });

    it('должен передавать placeholder="Поиск" в params', () => {
      render(<Filters />);
      expect(screen.getByTestId('form-field').getAttribute('data-placeholder')).toBe('Поиск');
    });

    it('должен передавать backgroundColor из colors.bgDark в style', () => {
      render(<Filters />);
      expect(screen.getByTestId('form-field').getAttribute('data-bg')).toBe('#f0f0f0');
    });

    it('должен передавать fontFamily из fontFamily.SBSansTextRegular в style', () => {
      render(<Filters />);
      expect(screen.getByTestId('form-field').getAttribute('data-font')).toBe('SBSansTextRegular');
    });

    it('должен рендерить кнопку настроек (suffix)', () => {
      render(<Filters />);
      expect(screen.getByTestId('settings-button')).toBeInTheDocument();
    });
  });

  describe('handleFilters', () => {
    it('должен вызвать handleFilters при клике на кнопку настроек', () => {
      const handleFilters = jest.fn();
      render(<Filters handleFilters={handleFilters} />);

      fireEvent.click(screen.getByTestId('settings-button'));

      expect(handleFilters).toHaveBeenCalledTimes(1);
    });

    it('должен корректно рендериться без handleFilters', () => {
      render(<Filters />);

      // Клик без ошибок
      expect(() => fireEvent.click(screen.getByTestId('settings-button'))).not.toThrow();
    });

    it('должен вызвать handleFilters ровно столько раз, сколько кликов', () => {
      const handleFilters = jest.fn();
      render(<Filters handleFilters={handleFilters} />);

      const button = screen.getByTestId('settings-button');
      fireEvent.click(button);
      fireEvent.click(button);
      fireEvent.click(button);

      expect(handleFilters).toHaveBeenCalledTimes(3);
    });
  });
});
