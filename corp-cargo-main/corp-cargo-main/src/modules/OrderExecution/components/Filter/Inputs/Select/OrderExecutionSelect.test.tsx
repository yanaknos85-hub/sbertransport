/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import '@testing-library/jest-dom'
import Select from '.'

// Мокаем зависимости
jest.mock('classnames', () => ({
  __esModule: true,
  default: jest.fn((...args) => args.filter(Boolean).join(' ')),
}))

// Мокаем иконку
jest.mock('./assets/DownArrowIcon', () => {
  return () => <span data-testid="down-arrow-icon">▼</span>
})

jest.mock('./select.module.scss', () => ({
  select: 'mock-select-class',
}))

// Улучшенный мок для Ant Design Select
jest.mock('antd', () => ({
  Select: jest.fn(({ 
    suffixIcon, 
    className, 
    children, 
    mode,
    onFocus,
    onBlur,
    onClear,
    allowClear,
    clearIcon,
    disabled,
    placeholder,
    value,
    defaultValue,
    showSearch,
    optionFilterProp,
    dropdownClassName,
    maxTagCount,
    id,
    'aria-label': ariaLabel,
    'aria-required': ariaRequired,
    style,
    name,
    options,
    ...rest 
  }) => {
    // Функция для правильного рендеринга suffixIcon
    const renderSuffixIcon = () => {
      if (!suffixIcon) return null
      // Если suffixIcon - это функция (компонент), вызываем её
      if (typeof suffixIcon === 'function') {
        return suffixIcon({})
      }
      // Если это React-элемент, возвращаем как есть
      return suffixIcon
    }

    return (
      <div 
        data-testid="antd-select" 
        className={className}
        data-mode={mode}
        data-allow-clear={allowClear}
        data-show-search={showSearch}
        data-option-filter-prop={optionFilterProp}
        data-dropdown-classname={dropdownClassName}
        data-max-tag-count={maxTagCount}
        data-disabled={disabled}
        data-value={value}
        data-name={name}
        id={id}
        aria-label={ariaLabel}
        aria-required={ariaRequired}
        style={style}
        onFocus={onFocus}
        onBlur={onBlur}
        {...rest}
      >
        <div data-testid="suffix-icon-container">{renderSuffixIcon()}</div>
        <select 
          data-testid="native-select"
          disabled={disabled}
          placeholder={placeholder}
          value={value || defaultValue || ''}
          name={name}
          onChange={() => {}} // Добавляем пустой onChange чтобы избежать warning
        >
          {children}
          {options?.map((opt: any) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
          <option value="option1">Option 1</option>
          <option value="option2">Option 2</option>
        </select>
        {allowClear && clearIcon && (
          <div data-testid="clear-icon" onClick={onClear}>
            {clearIcon}
          </div>
        )}
      </div>
    )
  }),
}))

describe('Select', () => {
  const mockClassName = jest.requireMock('classnames').default

  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('Базовый рендеринг', () => {
    it('должен рендерить компонент Select', () => {
      render(<Select />)
      expect(screen.getByTestId('antd-select')).toBeInTheDocument()
    })
  })

  describe('Стилизация', () => {
    it('должен применять стили из модуля', () => {
      render(<Select />)
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveClass('mock-select-class')
    })

    it('должен объединять переданный className с стилями модуля', () => {
      render(<Select className="custom-class" />)
      expect(mockClassName).toHaveBeenCalledWith(
        'mock-select-class',
        'custom-class'
      )
    })

    it('должен корректно обрабатывать несколько className', () => {
      render(<Select className="custom-class another-class" />)
      expect(mockClassName).toHaveBeenCalledWith(
        'mock-select-class',
        'custom-class another-class'
      )
    })

    it('должен работать без переданного className', () => {
      render(<Select />)
      expect(mockClassName).toHaveBeenCalledWith(
        'mock-select-class',
        undefined
      )
    })
  })

  describe('Различные режимы Select', () => {
    it('должен поддерживать mode="multiple"', () => {
      render(<Select mode="multiple" />)
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-mode', 'multiple')
    })

    it('должен поддерживать mode="tags"', () => {
      render(<Select mode="tags" />)
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-mode', 'tags')
    })

    it('должен поддерживать mode="combobox"', () => {
      render(<Select mode="combobox" />)
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-mode', 'combobox')
    })
  })

  describe('События', () => {
    it('должен вызывать onFocus при фокусе', () => {
      const handleFocus = jest.fn()
      render(<Select onFocus={handleFocus} />)
      const select = screen.getByTestId('antd-select')
      fireEvent.focus(select)
      expect(handleFocus).toHaveBeenCalledTimes(1)
    })

    it('должен вызывать onBlur при потере фокуса', () => {
      const handleBlur = jest.fn()
      render(<Select onBlur={handleBlur} />)
      const select = screen.getByTestId('antd-select')
      fireEvent.blur(select)
      expect(handleBlur).toHaveBeenCalledTimes(1)
    })
  })

  describe('Состояния Select', () => {
    it('должен отображаться в disabled состоянии', () => {
      render(<Select disabled />)
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-disabled', 'true')
    })

    it('должен отображаться с placeholder', () => {
      const placeholder = 'Выберите значение'
      render(<Select placeholder={placeholder} />)
      const nativeSelect = screen.getByTestId('native-select')
      expect(nativeSelect).toHaveAttribute('placeholder', placeholder)
    })

    it('должен отображаться с value', () => {
      render(<Select value="option2" />)
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-value', 'option2')
    })
  })

  describe('Функциональность поиска', () => {
    it('должен поддерживать optionFilterProp', () => {
      render(<Select showSearch optionFilterProp="label" />)
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-option-filter-prop', 'label')
      expect(select).toHaveAttribute('data-show-search', 'true')
    })
  })

  describe('Dropdown функциональность', () => {
    it('должен поддерживать dropdownClassName', () => {
      render(<Select dropdownClassName="custom-dropdown" />)
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-dropdown-classname', 'custom-dropdown')
    })
  })

  describe('Очистка значения', () => {
    it('должен поддерживать clearIcon', () => {
      const clearIcon = <span data-testid="custom-clear-icon">✗</span>
      render(<Select allowClear clearIcon={clearIcon} />)
      expect(screen.getByTestId('custom-clear-icon')).toBeInTheDocument()
    })
  })

  describe('MaxTag функциональность', () => {
    it('должен поддерживать maxTagCount', () => {
      render(<Select mode="multiple" maxTagCount={3} />)
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-max-tag-count', '3')
    })
  })

  describe('Краевые случаи', () => {
    it('должен обрабатывать пустые пропсы', () => {
      render(<Select />)
      const select = screen.getByTestId('antd-select')
      expect(select).toBeInTheDocument()
      expect(select).toHaveClass('mock-select-class')
    })

    it('должен обрабатывать undefined пропсы', () => {
      render(<Select value={undefined} placeholder={undefined} />)
      const select = screen.getByTestId('antd-select')
      expect(select).toBeInTheDocument()
    })

    it('должен обрабатывать null пропсы', () => {
      render(<Select value={null as any} placeholder={null as any} />)
      const select = screen.getByTestId('antd-select')
      expect(select).toBeInTheDocument()
    })

    it('должен корректно работать с пустым массивом options', () => {
      render(<Select options={[]} />)
      const select = screen.getByTestId('antd-select')
      expect(select).toBeInTheDocument()
    })
  })

  describe('Дополнительные атрибуты', () => {
    it('должен пробрасывать id', () => {
      render(<Select id="custom-select-id" />)
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('id', 'custom-select-id')
    })

    it('должен пробрасывать aria-атрибуты', () => {
      render(<Select aria-label="test select" aria-required="true" />)
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('aria-label', 'test select')
      expect(select).toHaveAttribute('aria-required', 'true')
    })

    it('должен пробрасывать style', () => {
      render(<Select style={{ width: '200px', margin: '10px' }} />)
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveStyle({ width: '200px', margin: '10px' })
    })
  })

  describe('Интеграция с Form', () => {
    it('должен работать внутри формы', () => {
      const handleSubmit = jest.fn((e) => e.preventDefault())
      
      render(
        <form onSubmit={handleSubmit}>
          <Select name="category" placeholder="Выберите категорию" />
          <button type="submit">Submit</button>
        </form>
      )

      const select = screen.getByTestId('antd-select')
      const submitButton = screen.getByText('Submit')

      expect(select).toHaveAttribute('data-name', 'category')
      fireEvent.click(submitButton)
      expect(handleSubmit).toHaveBeenCalled()
    })
  })
})