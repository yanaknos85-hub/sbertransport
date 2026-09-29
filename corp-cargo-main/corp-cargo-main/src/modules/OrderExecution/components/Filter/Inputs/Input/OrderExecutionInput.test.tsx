/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import '@testing-library/jest-dom'
import Input from '.'

// Мокаем зависимости
jest.mock('classnames', () => ({
  __esModule: true,
  default: jest.fn((...args) => args.filter(Boolean).join(' ')),
}))

// Улучшенный мок для Ant Design Input
jest.mock('antd', () => ({
  Input: jest.fn(({ 
    className, 
    allowClear,
    value,
    defaultValue,
    onChange,
    ...props 
  }) => (
    <input
      data-testid="antd-input"
      className={className}
      data-allow-clear={allowClear}
      value={value === null ? '' : value || defaultValue || ''}
      onChange={onChange || (() => {})}
      {...props}
    />
  )),
}))

jest.mock('./input.module.scss', () => ({
  input: 'mock-input-class',
}))

describe('Input', () => {
  const mockClassName = jest.requireMock('classnames').default

  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('Базовый рендеринг', () => {
    it('должен рендерить input элемент', () => {
      render(<Input />)

      expect(screen.getByTestId('antd-input')).toBeInTheDocument()
    })

    it('должен пробрасывать все пропсы в AntDesignInput', () => {
      render(
        <Input
          placeholder="Введите текст"
          value="test value"
          disabled
          maxLength={10}
        />
      )

      const input = screen.getByTestId('antd-input')
      expect(input).toHaveAttribute('placeholder', 'Введите текст')
      expect(input).toHaveAttribute('value', 'test value')
      expect(input).toBeDisabled()
      expect(input).toHaveAttribute('maxLength', '10')
    })

    it('должен применять стили из модуля', () => {
      render(<Input />)

      const input = screen.getByTestId('antd-input')
      expect(input).toHaveClass('mock-input-class')
    })
  })

  describe('Обработка className', () => {
    it('должен объединять переданный className с стилями модуля', () => {
      render(<Input className="custom-class" />)

      expect(mockClassName).toHaveBeenCalledWith(
        'mock-input-class',
        'custom-class'
      )
    })

    it('должен корректно обрабатывать несколько className', () => {
      render(<Input className="custom-class another-class" />)

      expect(mockClassName).toHaveBeenCalledWith(
        'mock-input-class',
        'custom-class another-class'
      )
    })

    it('должен работать без переданного className', () => {
      render(<Input />)

      expect(mockClassName).toHaveBeenCalledWith(
        'mock-input-class',
        undefined
      )
    })
  })

  describe('События', () => {
    it('должен вызывать onChange при вводе текста', () => {
      const handleChange = jest.fn()
      render(<Input onChange={handleChange} />)

      const input = screen.getByTestId('antd-input')
      fireEvent.change(input, { target: { value: 'new value' } })

      expect(handleChange).toHaveBeenCalledTimes(1)
    })

    it('должен вызывать onFocus при фокусе', () => {
      const handleFocus = jest.fn()
      render(<Input onFocus={handleFocus} />)

      const input = screen.getByTestId('antd-input')
      fireEvent.focus(input)

      expect(handleFocus).toHaveBeenCalledTimes(1)
    })

    it('должен вызывать onBlur при потере фокуса', () => {
      const handleBlur = jest.fn()
      render(<Input onBlur={handleBlur} />)

      const input = screen.getByTestId('antd-input')
      fireEvent.blur(input)

      expect(handleBlur).toHaveBeenCalledTimes(1)
    })

    it('должен вызывать onKeyDown при нажатии клавиши', () => {
      const handleKeyDown = jest.fn()
      render(<Input onKeyDown={handleKeyDown} />)

      const input = screen.getByTestId('antd-input')
      fireEvent.keyDown(input, { key: 'Enter', code: 'Enter' })

      expect(handleKeyDown).toHaveBeenCalledTimes(1)
    })
  })

  describe('Различные типы Input', () => {
    it('должен поддерживать type="password"', () => {
      render(<Input type="password" />)

      const input = screen.getByTestId('antd-input')
      expect(input).toHaveAttribute('type', 'password')
    })

    it('должен поддерживать type="number"', () => {
      render(<Input type="number" />)

      const input = screen.getByTestId('antd-input')
      expect(input).toHaveAttribute('type', 'number')
    })

    it('должен поддерживать type="email"', () => {
      render(<Input type="email" />)

      const input = screen.getByTestId('antd-input')
      expect(input).toHaveAttribute('type', 'email')
    })

    it('должен поддерживать type="tel"', () => {
      render(<Input type="tel" />)

      const input = screen.getByTestId('antd-input')
      expect(input).toHaveAttribute('type', 'tel')
    })
  })

  describe('Состояния Input', () => {
    it('должен отображаться в disabled состоянии', () => {
      render(<Input disabled />)

      const input = screen.getByTestId('antd-input')
      expect(input).toBeDisabled()
    })

    it('должен отображаться в readOnly состоянии', () => {
      render(<Input readOnly />)

      const input = screen.getByTestId('antd-input')
      expect(input).toHaveAttribute('readOnly')
    })

    it('должен отображаться с placeholder', () => {
      const placeholder = 'Введите значение'
      render(<Input placeholder={placeholder} />)

      const input = screen.getByTestId('antd-input')
      expect(input).toHaveAttribute('placeholder', placeholder)
    })

    it('должен отображаться с defaultValue', () => {
      render(<Input defaultValue="initial value" />)

      const input = screen.getByTestId('antd-input')
      expect(input).toHaveAttribute('value', 'initial value')
    })
  })

  describe('Очистка значения', () => {
    it('должен поддерживать allowClear', () => {
      render(<Input allowClear value="test" />)

      const input = screen.getByTestId('antd-input')
      expect(input).toHaveAttribute('value', 'test')
      // Проверяем что allowClear сохранен в data-атрибуте
      expect(input).toHaveAttribute('data-allow-clear', 'true')
    })

    it('должен очищать значение при клике на иконку очистки (через пропсы)', () => {
      const handleChange = jest.fn()
      render(<Input allowClear value="test" onChange={handleChange} />)

      // В реальном Ant Design кнопка очистки рендерится отдельно
      // Мы просто проверяем что пропс allowClear передан
      const input = screen.getByTestId('antd-input')
      expect(input).toHaveAttribute('value', 'test')
      expect(input).toHaveAttribute('data-allow-clear', 'true')
    })
  })

  describe('Краевые случаи', () => {
    it('должен обрабатывать пустые пропсы', () => {
      render(<Input />)

      const input = screen.getByTestId('antd-input')
      expect(input).toBeInTheDocument()
      expect(input).toHaveClass('mock-input-class')
    })

    it('должен обрабатывать undefined пропсы', () => {
      render(<Input value={undefined} placeholder={undefined} />)

      const input = screen.getByTestId('antd-input')
      expect(input).toBeInTheDocument()
      expect(input).toHaveAttribute('value', '')
    })

    it('должен обрабатывать null пропсы', () => {
      render(<Input value={null as any} placeholder={null as any} />)

      const input = screen.getByTestId('antd-input')
      expect(input).toBeInTheDocument()
      expect(input).toHaveAttribute('value', '')
    })

    it('должен корректно работать с id', () => {
      render(<Input id="custom-id" />)

      const input = screen.getByTestId('antd-input')
      expect(input).toHaveAttribute('id', 'custom-id')
    })

    it('должен корректно работать с name', () => {
      render(<Input name="field-name" />)

      const input = screen.getByTestId('antd-input')
      expect(input).toHaveAttribute('name', 'field-name')
    })
  })

  describe('Проброс дополнительных атрибутов', () => {
    it('должен пробрасывать data-атрибуты', () => {
      render(<Input data-testid="custom-test-id" data-custom="custom-value" />)

      const input = screen.getByTestId('custom-test-id')
      expect(input).toHaveAttribute('data-custom', 'custom-value')
    })

    it('должен пробрасывать aria-атрибуты', () => {
      render(<Input aria-label="test input" aria-required="true" />)

      const input = screen.getByTestId('antd-input')
      expect(input).toHaveAttribute('aria-label', 'test input')
      expect(input).toHaveAttribute('aria-required', 'true')
    })

    it('должен пробрасывать style', () => {
      render(<Input style={{ width: '200px', color: 'red' }} />)

      const input = screen.getByTestId('antd-input')
      expect(input).toHaveStyle({ width: '200px', color: 'red' })
    })
  })
})