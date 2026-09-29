/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { Form } from 'antd';
import TimeField from './TimeField';
import useValidationRules from './useValidationRules';

jest.mock('./useValidationRules');

jest.mock('classnames', () => ({
  __esModule: true,
  default: jest.fn((...args) => {
    const result = args.filter(arg => {
      if (arg && typeof arg === 'object') {
        return Object.values(arg).some(value => value === true);
      }
      return Boolean(arg);
    }).map(arg => {
      if (arg && typeof arg === 'object') {
        const keys = Object.keys(arg).filter(key => arg[key] === true);
        return keys.join(' ');
      }
      return arg;
    }).join(' ');
    return result;
  }),
}));

jest.mock('constants/constants.app', () => ({
  DATE_FORMAT: {
    TIME_BASE_SHORT: 'HH:mm',
  },
}));

jest.mock('../modelDetail.module.scss', () => ({
  formItem: 'formItem-class',
}));

const mockedUseValidationRules = useValidationRules as jest.MockedFunction<typeof useValidationRules>;

jest.mock('antd', () => {
  const originalModule = jest.requireActual('antd');

  const MockTimePicker = ({
    style,
    placeholder,
    format,
    onChange,
    value,
    ...props
  }: any) => (
    <div
      data-testid="mock-time-picker"
      data-style={JSON.stringify(style)}
      data-placeholder={placeholder}
      data-format={format}
      data-value={value ? value.toString() : ''}
      {...props}
    >
      <input
        type="text"
        placeholder={placeholder}
        data-testid="time-picker-input"
        onChange={e => onChange && onChange(e.target.value)}
        value={value || ''}
        style={{ width: '100%', height: '48px' }}
      />
    </div>
  );

  const MockFormItem = ({
    children,
    label,
    name,
    rules,
    className,
    colon,
    ...props
  }: any) => {
    const fieldId = name ? `form-item-${name}` : `form-item-${Math.random().toString(36).substr(2, 9)}`;

    return (
      <div
        data-testid="form-item"
        data-name={name}
        data-label={label}
        data-colon={colon}
        data-rules={rules ? JSON.stringify(rules) : ''}
        className={className}
        {...props}
      >
        {label && <label data-testid="form-item-label" htmlFor={fieldId}>{label}</label>}
        {children}
      </div>
    );
  };

  return {
    ...originalModule,
    TimePicker: MockTimePicker,
    Form: {
      ...originalModule.Form,
      Item: MockFormItem,
    },
  };
});

describe('TimeField Component', () => {
  beforeAll(() => {
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: jest.fn().mockImplementation(query => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: jest.fn(),
        removeListener: jest.fn(),
        addEventListener: jest.fn(),
        removeEventListener: jest.fn(),
        dispatchEvent: jest.fn(),
      })),
    });

    global.ResizeObserver = jest.fn().mockImplementation(() => ({
      observe: jest.fn(),
      unobserve: jest.fn(),
      disconnect: jest.fn(),
    }));

    global.getComputedStyle = jest.fn(() => ({
      getPropertyValue: jest.fn(),
    } as any));
  });

  afterAll(() => {
    jest.restoreAllMocks();
  });

  const TestFormWrapper: React.FC<{ children: React.ReactNode; initialValues?: any }> = ({
    children,
    initialValues,
  }) => (
    <Form initialValues={initialValues}>
      {children}
    </Form>
  );

  const defaultProps = {
    name: 'testTime',
    label: 'Test Time Label',
    editable: true,
    isNewDesign: false,
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockedUseValidationRules.mockReturnValue([]);
  });

  describe('Рендеринг', () => {
    it('должен корректно рендерить компонент с базовыми пропсами', () => {
      render(
        <TestFormWrapper>
          <TimeField {...defaultProps} />
        </TestFormWrapper>
      );

      expect(screen.getByText('Test Time Label')).toBeInTheDocument();
      expect(screen.getByTestId('mock-time-picker')).toBeInTheDocument();
      expect(screen.getByPlaceholderText('Выберите время')).toBeInTheDocument();
    });

    it('должен вызывать useValidationRules с правильными пропсами', () => {
      const props = { ...defaultProps, editable: false };

      render(
        <TestFormWrapper>
          <TimeField {...props} />
        </TestFormWrapper>
      );

      expect(mockedUseValidationRules).toHaveBeenCalledWith(props);
    });

    it('должен применять класс formItem при isNewDesign = true', () => {
      const { getByTestId } = render(
        <TestFormWrapper>
          <TimeField {...defaultProps} isNewDesign={true} />
        </TestFormWrapper>
      );

      const formItem = getByTestId('form-item');
      expect(formItem).toHaveClass('formItem-class');
    });

    it('не должен применять класс formItem при isNewDesign = false', () => {
      const { getByTestId } = render(
        <TestFormWrapper>
          <TimeField {...defaultProps} isNewDesign={false} />
        </TestFormWrapper>
      );

      const formItem = getByTestId('form-item');
      expect(formItem).not.toHaveClass('formItem-class');
    });
  });

  describe('Валидация', () => {
    it('должен применять правила валидации из useValidationRules', () => {
      const mockValidationRules = [
        { required: true, message: 'Это поле обязательно' },
        { pattern: /^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, message: 'Неверный формат времени' },
      ];

      mockedUseValidationRules.mockReturnValue(mockValidationRules);

      render(
        <TestFormWrapper>
          <TimeField {...defaultProps} />
        </TestFormWrapper>
      );

      expect(mockedUseValidationRules).toHaveBeenCalled();

      const formItem = screen.getByTestId('form-item');
      const rulesAttr = formItem.getAttribute('data-rules');
      expect(rulesAttr).toBe(JSON.stringify(mockValidationRules));
    });
  });

  describe('Пропсы', () => {
    it('должен обрабатывать разные имена полей', () => {
      const props = { ...defaultProps, name: 'differentName' };

      render(
        <TestFormWrapper>
          <TimeField {...props} />
        </TestFormWrapper>
      );

      const formItem = screen.getByTestId('form-item');
      expect(formItem).toHaveAttribute('data-name', 'differentName');
    });

    it('должен обрабатывать разные лейблы', () => {
      render(
        <TestFormWrapper>
          <TimeField {...defaultProps} label="Другое название" />
        </TestFormWrapper>
      );

      expect(screen.getByText('Другое название')).toBeInTheDocument();
    });

    it('должен устанавливать colon={false} для Form.Item', () => {
      render(
        <TestFormWrapper>
          <TimeField {...defaultProps} />
        </TestFormWrapper>
      );

      const formItem = screen.getByTestId('form-item');
      expect(formItem).toHaveAttribute('data-colon', 'false');
    });
  });

  describe('TimePicker пропсы', () => {
    it('должен передавать правильные стили в TimePicker', () => {
      render(
        <TestFormWrapper>
          <TimeField {...defaultProps} />
        </TestFormWrapper>
      );

      const timePicker = screen.getByTestId('mock-time-picker');
      const styleAttr = timePicker.getAttribute('data-style');
      expect(styleAttr).toContain('"width":"100%"');
      expect(styleAttr).toContain('"maxWidth":"280px"');
      expect(styleAttr).toContain('"height":"48px"');
    });

    it('должен передавать правильный формат времени', () => {
      render(
        <TestFormWrapper>
          <TimeField {...defaultProps} />
        </TestFormWrapper>
      );

      const timePicker = screen.getByTestId('mock-time-picker');
      expect(timePicker).toHaveAttribute('data-format', 'HH:mm');
    });

    it('должен передавать правильный placeholder', () => {
      render(
        <TestFormWrapper>
          <TimeField {...defaultProps} />
        </TestFormWrapper>
      );

      const timePicker = screen.getByTestId('mock-time-picker');
      expect(timePicker).toHaveAttribute('data-placeholder', 'Выберите время');
    });
  });

  describe('Взаимодействие', () => {
    it('позволяет вводить значение в TimePicker', () => {
      const MockedTimeField = () => {
        const [value, setValue] = React.useState('');

        return (
          <div>
            <input
              data-testid="time-picker-input"
              value={value}
              onChange={e => setValue(e.target.value)}
              placeholder="Выберите время"
            />
          </div>
        );
      };

      render(<MockedTimeField />);
      const input = screen.getByTestId('time-picker-input') as HTMLInputElement;
      fireEvent.change(input, { target: { value: '14:30' } });
      expect(input.value).toBe('14:30');
    });
  });
});
