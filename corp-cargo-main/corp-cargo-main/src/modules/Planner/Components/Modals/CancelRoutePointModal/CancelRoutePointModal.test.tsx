import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { CancelRoutePointModal } from './CancelRoutePointModal';

// ========================
//        Моки
// ========================

jest.mock('../Container', () => ({
  Container: ({
    children,
    visible,
    onCancel,
    width,
    centered,
    footer,
  }: {
    children: React.ReactNode;
    visible: boolean;
    onCancel: () => void;
    width?: number;
    centered?: boolean;
    footer?: unknown;
  }) => (
    <div
      data-testid="container"
      data-visible={visible ? 'true' : 'false'}
      data-width={width ?? ''}
      data-centered={centered ? 'true' : 'false'}
      data-footer={footer === null ? 'null' : 'set'}
    >
      <button data-testid="container-cancel" type="button" onClick={onCancel}>
        X
      </button>
      {visible && <div data-testid="container-children">{children}</div>}
    </div>
  ),
}));

jest.mock('./CancelRoutePointModal.styles', () => ({
  __esModule: true,
  ModalContainer: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="modal-container">{children}</div>
  ),
  Title: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="modal-title">{children}</div>
  ),
  Text: ({ children }: { children: React.ReactNode }) => (
    <p data-testid="modal-text">{children}</p>
  ),
  ButtonsBlock: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="buttons-block">{children}</div>
  ),
  ButtonCancel: ({
    children,
    onClick,
    cancel,
  }: {
    children: React.ReactNode;
    onClick?: () => void;
    cancel?: boolean;
  }) => (
    <button
      data-testid="cancel-button"
      data-cancel={cancel ? 'true' : 'false'}
      type="button"
      onClick={onClick}
    >
      {children}
    </button>
  ),
}));

// ButtonOk объявлен в самом файле как styled(ButtonAnt) — мокаем antd
jest.mock('antd', () => {
  const React = require('react');
  return {
    Button: ({
      children,
      onClick,
      ...rest
    }: {
      children: React.ReactNode;
      onClick?: () => void;
    } & Record<string, unknown>) => (
      <button
        data-testid="ok-button"
        type="button"
        onClick={onClick}
        data-rest={JSON.stringify(rest)}
      >
        {children}
      </button>
    ),
  };
});

jest.mock('shared/styles/styles', () => ({
  colors: {
    white: '#ffffff',
    gray10: '#222222',
    gray9: '#333333',
    gray7: '#666666',
    solidBodyNormal: '#10bf6a',
    normalOff: '#cccccc',
    bgDark: '#f0f0f0',
  },
}));

// ========================
//       Тесты
// ========================

describe('CancelRoutePointModal', () => {
  describe('начальное состояние', () => {
    it('должен рендерить Container', () => {
      render(<CancelRoutePointModal />);
      expect(screen.getByTestId('container')).toBeInTheDocument();
    });

    it('должен начинать с visible=true', () => {
      render(<CancelRoutePointModal />);
      expect(screen.getByTestId('container').getAttribute('data-visible')).toBe('true');
    });

    it('должен передавать width=500 в Container', () => {
      render(<CancelRoutePointModal />);
      expect(screen.getByTestId('container').getAttribute('data-width')).toBe('500');
    });

    it('должен передавать centered=true в Container', () => {
      render(<CancelRoutePointModal />);
      expect(screen.getByTestId('container').getAttribute('data-centered')).toBe('true');
    });

    it('должен передавать footer=null в Container', () => {
      render(<CancelRoutePointModal />);
      expect(screen.getByTestId('container').getAttribute('data-footer')).toBe('null');
    });
  });

  describe('содержимое модалки', () => {
    it('должен рендерить ModalContainer', () => {
      render(<CancelRoutePointModal />);
      expect(screen.getByTestId('modal-container')).toBeInTheDocument();
    });

    it('должен рендерить заголовок "Удалить заявку?"', () => {
      render(<CancelRoutePointModal />);
      expect(screen.getByTestId('modal-title')).toHaveTextContent('Удалить заявку?');
    });

    it('должен рендерить текст предупреждения', () => {
      render(<CancelRoutePointModal />);
      const expectedText = 'При удалении точки сбора Звенигордская 32, вы также удалите точку доставки Тверская 4';
      expect(screen.getByTestId('modal-text')).toHaveTextContent(expectedText);
    });

    it('должен рендерить блок кнопок', () => {
      render(<CancelRoutePointModal />);
      expect(screen.getByTestId('buttons-block')).toBeInTheDocument();
    });

    it('должен рендерить кнопку Отменить', () => {
      render(<CancelRoutePointModal />);
      expect(screen.getByTestId('cancel-button')).toBeInTheDocument();
      expect(screen.getByTestId('cancel-button')).toHaveTextContent('Отменить');
    });

    it('должен рендерить кнопку Создать (ButtonOk)', () => {
      render(<CancelRoutePointModal />);
      expect(screen.getByTestId('ok-button')).toBeInTheDocument();
      expect(screen.getByTestId('ok-button')).toHaveTextContent('Создать');
    });
  });

  describe('ButtonCancel (styled)', () => {
    it('должен передавать проп cancel=true', () => {
      render(<CancelRoutePointModal />);
      expect(screen.getByTestId('cancel-button').getAttribute('data-cancel')).toBe('true');
    });
  });

  describe('handleCancel', () => {
    it('должен закрыть модалку при клике на "Отменить"', () => {
      render(<CancelRoutePointModal />);
      expect(screen.getByTestId('container').getAttribute('data-visible')).toBe('true');

      fireEvent.click(screen.getByTestId('cancel-button'));

      expect(screen.getByTestId('container').getAttribute('data-visible')).toBe('false');
    });

    it('должен закрыть модалку при клике на крестик Container', () => {
      render(<CancelRoutePointModal />);
      expect(screen.getByTestId('container').getAttribute('data-visible')).toBe('true');

      fireEvent.click(screen.getByTestId('container-cancel'));

      expect(screen.getByTestId('container').getAttribute('data-visible')).toBe('false');
    });
  });
});
