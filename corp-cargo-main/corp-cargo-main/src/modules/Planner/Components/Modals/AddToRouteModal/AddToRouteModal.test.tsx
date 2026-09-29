import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AddToRouteModal } from './AddToRouteModal';

// ========================
//        Моки
// ========================

const mockHandleAddToRoute = jest.fn();

jest.mock('modules/Planner/context/PlannerContext', () => ({
  usePlanner: () => ({
    handleAddToRoute: mockHandleAddToRoute,
  }),
}));

jest.mock('shared/form/FormField/FormField', () => {
  const MockFormField = (props: {
    name?: string;
    label?: string;
    type?: string;
    params?: { options?: Array<{ label: string; value: string }> };
    rules?: unknown[];
  }) => (
    <div
      data-testid="form-field"
      data-name={props.name ?? ''}
      data-label={props.label ?? ''}
      data-type={props.type ?? ''}
      data-options-count={props.params?.options?.length ?? 0}
      data-has-rules={(props.rules?.length ?? 0) > 0 ? 'true' : 'false'}
    />
  );
  return { __esModule: true, default: MockFormField };
});

jest.mock('shared/fieldValidationRules', () => ({
  ValidationRules: {
    general: {
      required: { required: true, message: 'Required' },
    },
  },
}));

jest.mock('shared/form/Field/Field', () => ({
  FieldType: {
    input: 'input',
    textarea: 'textarea',
    number: 'number',
    select: 'select',
  },
}));

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
      {children}
    </div>
  ),
}));

jest.mock('./AddToRouteModal.styles', () => ({
  __esModule: true,
  ModalContainer: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="modal-container">{children}</div>
  ),
  Title: ({ children }: { children: React.ReactNode }) => <div data-testid="modal-title">{children}</div>,
  Text: ({ children }: { children: React.ReactNode }) => <p data-testid="modal-text">{children}</p>,
  ButtonsBlock: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="buttons-block">{children}</div>
  ),
  ButtonStyled: ({
    children,
    onClick,
  }: {
    children: React.ReactNode;
    onClick?: () => void;
  }) => (
    <button data-testid="add-button" type="button" onClick={onClick}>
      {children}
    </button>
  ),
  ButtonOkStyled: ({
    children,
    onClick,
  }: {
    children: React.ReactNode;
    onClick?: () => void;
  }) => (
    <button data-testid="cancel-button" type="button" onClick={onClick}>
      {children}
    </button>
  ),
}));

// ========================
//   Тестовая обёртка
// ========================

const renderModal = (props: Partial<React.ComponentProps<typeof AddToRouteModal>> = {}) => {
  const defaultProps = {
    visible: true,
    handleCancel: jest.fn(),
    ...props,
  };
  return render(
    <MemoryRouter>
      <AddToRouteModal {...defaultProps} />
    </MemoryRouter>,
  );
};

// ========================
//       Тесты
// ========================

describe('AddToRouteModal', () => {
  beforeEach(() => {
    mockHandleAddToRoute.mockClear();
  });

  describe('рендеринг', () => {
    it('должен рендерить Container', () => {
      renderModal();
      expect(screen.getByTestId('container')).toBeInTheDocument();
    });

    it('должен рендерить ModalContainer', () => {
      renderModal();
      expect(screen.getByTestId('modal-container')).toBeInTheDocument();
    });

    it('должен рендерить заголовок (title)', () => {
      renderModal({ title: 'Добавить в маршрут' });
      expect(screen.getByTestId('modal-title')).toHaveTextContent('Добавить в маршрут');
    });

    it('должен рендерить текст (text)', () => {
      renderModal({ text: 'Выберите маршрут для добавления' });
      expect(screen.getByTestId('modal-text')).toHaveTextContent('Выберите маршрут для добавления');
    });

    it('должен НЕ рендерить заголовок если title не передан', () => {
      renderModal();
      expect(screen.getByTestId('modal-title')).toBeEmptyDOMElement();
    });

    it('должен НЕ рендерить текст если text не передан', () => {
      renderModal();
      expect(screen.getByTestId('modal-text')).toBeEmptyDOMElement();
    });

    it('должен передавать visible в Container', () => {
      renderModal({ visible: true });
      expect(screen.getByTestId('container').getAttribute('data-visible')).toBe('true');
    });

    it('должен передавать visible=false в Container', () => {
      renderModal({ visible: false });
      expect(screen.getByTestId('container').getAttribute('data-visible')).toBe('false');
    });

    it('должен передавать width=440 в Container', () => {
      renderModal();
      expect(screen.getByTestId('container').getAttribute('data-width')).toBe('440');
    });

    it('должен передавать centered в Container', () => {
      renderModal();
      expect(screen.getByTestId('container').getAttribute('data-centered')).toBe('true');
    });

    it('должен передавать footer=null в Container', () => {
      renderModal();
      expect(screen.getByTestId('container').getAttribute('data-footer')).toBe('null');
    });

    it('должен рендерить кнопки Отменить и Добавить', () => {
      renderModal();
      expect(screen.getByTestId('cancel-button')).toBeInTheDocument();
      expect(screen.getByTestId('add-button')).toBeInTheDocument();
    });
  });

  describe('FormField (route)', () => {
    it('должен рендерить FormField когда isNew=false (по умолчанию)', () => {
      renderModal();
      expect(screen.getByTestId('form-field')).toBeInTheDocument();
    });

    it('должен передавать name="route"', () => {
      renderModal();
      expect(screen.getByTestId('form-field').getAttribute('data-name')).toBe('route');
    });

    it('должен передавать label="Маршрут"', () => {
      renderModal();
      expect(screen.getByTestId('form-field').getAttribute('data-label')).toBe('Маршрут');
    });

    it('должен передавать type="select"', () => {
      renderModal();
      expect(screen.getByTestId('form-field').getAttribute('data-type')).toBe('select');
    });

    it('должен передавать 4 опции', () => {
      renderModal();
      expect(screen.getByTestId('form-field').getAttribute('data-options-count')).toBe('4');
    });

    it('должен передавать rules с required', () => {
      renderModal();
      expect(screen.getByTestId('form-field').getAttribute('data-has-rules')).toBe('true');
    });

    it('НЕ должен рендерить FormField когда isNew=true', () => {
      renderModal({ isNew: true });
      expect(screen.queryByTestId('form-field')).toBeNull();
    });
  });

  describe('обработчики', () => {
    it('должен вызвать handleCancel при клике на Отменить', () => {
      const handleCancel = jest.fn();
      renderModal({ handleCancel });

      fireEvent.click(screen.getByTestId('cancel-button'));

      expect(handleCancel).toHaveBeenCalledTimes(1);
    });

    it('должен вызвать handleAddToRoute при клике на Добавить', () => {
      renderModal();

      fireEvent.click(screen.getByTestId('add-button'));

      expect(mockHandleAddToRoute).toHaveBeenCalledTimes(1);
    });

    it('должен вызвать handleCancel при клике на крестик Container', () => {
      const handleCancel = jest.fn();
      renderModal({ handleCancel });

      fireEvent.click(screen.getByTestId('container-cancel'));

      expect(handleCancel).toHaveBeenCalledTimes(1);
    });
  });

  describe('Link (Добавить)', () => {
    it('должен рендерить ссылку с href="/planner/plannerRoutes"', () => {
      renderModal();
      const link = screen.getByTestId('add-button').closest('a');
      expect(link).toHaveAttribute('href', '/planner/plannerRoutes');
    });
  });
});
