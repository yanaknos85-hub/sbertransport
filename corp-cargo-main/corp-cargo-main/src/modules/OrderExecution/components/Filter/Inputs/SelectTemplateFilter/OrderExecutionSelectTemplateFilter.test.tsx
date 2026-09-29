/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import '@testing-library/jest-dom'
import { SelectTemplateFilter } from '.'

// Мокаем зависимости
jest.mock('classnames', () => ({
  __esModule: true,
  default: jest.fn((...args) => args.filter(Boolean).join(' ')),
}))

jest.mock('antd', () => ({
  Select: jest.fn(({ 
    options,
    suffixIcon,
    className,
    mode,
    onFocus,
    onBlur,
    onChange,
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
        onChange={onChange}
        {...rest}
      >
        <div data-testid="suffix-icon-container">{renderSuffixIcon()}</div>
        <select 
          data-testid="native-select"
          disabled={disabled}
          placeholder={placeholder}
          value={value || defaultValue || ''}
          name={name}
          onChange={onChange || (() => {})}
        >
          {options?.map((opt: any) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
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

// Мокаем иконку
jest.mock('./assets/DownArrowIcon', () => {
  return function MockDownArrowIcon() {
    return <span data-testid="down-arrow-icon">▼</span>
  }
})

jest.mock('../Select/select.module.scss', () => ({
  select: 'mock-select-class',
}))

describe('SelectTemplateFilter', () => {
  const mockClassName = jest.requireMock('classnames').default
  const mockOptions = [
    { value: 'option1', label: 'Option 1' },
    { value: 'option2', label: 'Option 2' },
    { value: 'option3', label: 'Option 3' },
  ]

  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('Базовый рендеринг', () => {
    it('должен рендерить компонент Select', () => {
      render(<SelectTemplateFilter options={mockOptions} />)
      expect(screen.getByTestId('antd-select')).toBeInTheDocument()
    })

    it('должен рендерить опции из пропса options', () => {
      render(<SelectTemplateFilter options={mockOptions} />)
      
      const nativeSelect = screen.getByTestId('native-select')
      expect(nativeSelect).toBeInTheDocument()
      
      const options = nativeSelect.querySelectorAll('option')
      expect(options).toHaveLength(3)
      expect(options[0]).toHaveValue('option1')
      expect(options[0]).toHaveTextContent('Option 1')
      expect(options[1]).toHaveValue('option2')
      expect(options[1]).toHaveTextContent('Option 2')
      expect(options[2]).toHaveValue('option3')
      expect(options[2]).toHaveTextContent('Option 3')
    })

    it('должен использовать иконку DownArrowIcon как suffixIcon', () => {
      render(<SelectTemplateFilter options={mockOptions} />)
      
      expect(screen.getByTestId('down-arrow-icon')).toBeInTheDocument()
      expect(screen.getByTestId('suffix-icon-container')).toContainElement(
        screen.getByTestId('down-arrow-icon')
      )
    })
  })

  describe('Стилизация', () => {
    it('должен применять стили из модуля', () => {
      render(<SelectTemplateFilter options={mockOptions} />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveClass('mock-select-class')
    })

    it('должен объединять переданный className с стилями модуля', () => {
      render(<SelectTemplateFilter options={mockOptions} className="custom-class" />)
      
      expect(mockClassName).toHaveBeenCalledWith(
        'mock-select-class',
        'custom-class'
      )
    })

    it('должен корректно обрабатывать несколько className', () => {
      render(<SelectTemplateFilter options={mockOptions} className="custom-class another-class" />)
      
      expect(mockClassName).toHaveBeenCalledWith(
        'mock-select-class',
        'custom-class another-class'
      )
    })

    it('должен работать без переданного className', () => {
      render(<SelectTemplateFilter options={mockOptions} />)
      
      expect(mockClassName).toHaveBeenCalledWith(
        'mock-select-class',
        undefined
      )
    })
  })

  describe('Проброс пропсов в AntDesignSelect', () => {
    it('должен пробрасывать mode', () => {
      render(<SelectTemplateFilter options={mockOptions} mode="multiple" />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-mode', 'multiple')
    })

    it('должен пробрасывать placeholder', () => {
      const placeholder = 'Выберите значение'
      render(<SelectTemplateFilter options={mockOptions} placeholder={placeholder} />)
      
      const nativeSelect = screen.getByTestId('native-select')
      expect(nativeSelect).toHaveAttribute('placeholder', placeholder)
    })

    it('должен пробрасывать value', () => {
      render(<SelectTemplateFilter options={mockOptions} value="option2" />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-value', 'option2')
    })

    it('должен пробрасывать disabled', () => {
      render(<SelectTemplateFilter options={mockOptions} disabled />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-disabled', 'true')
      const nativeSelect = screen.getByTestId('native-select')
      expect(nativeSelect).toBeDisabled()
    })

    it('должен пробрасывать allowClear', () => {
      render(<SelectTemplateFilter options={mockOptions} allowClear />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-allow-clear', 'true')
    })

    it('должен пробрасывать showSearch', () => {
      render(<SelectTemplateFilter options={mockOptions} showSearch />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-show-search', 'true')
    })

    it('должен пробрасывать optionFilterProp', () => {
      render(<SelectTemplateFilter options={mockOptions} showSearch optionFilterProp="label" />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-option-filter-prop', 'label')
    })

    it('должен пробрасывать maxTagCount', () => {
      render(<SelectTemplateFilter options={mockOptions} mode="multiple" maxTagCount={2} />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-max-tag-count', '2')
    })

    it('должен пробрасывать dropdownClassName', () => {
      render(<SelectTemplateFilter options={mockOptions} dropdownClassName="custom-dropdown" />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-dropdown-classname', 'custom-dropdown')
    })

    it('должен пробрасывать id', () => {
      render(<SelectTemplateFilter options={mockOptions} id="custom-select-id" />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('id', 'custom-select-id')
    })

    it('должен пробрасывать name', () => {
      render(<SelectTemplateFilter options={mockOptions} name="category" />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-name', 'category')
      const nativeSelect = screen.getByTestId('native-select')
      expect(nativeSelect).toHaveAttribute('name', 'category')
    })

    it('должен пробрасывать style', () => {
      render(<SelectTemplateFilter options={mockOptions} style={{ width: '300px', margin: '10px' }} />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveStyle({ width: '300px', margin: '10px' })
    })

    it('должен пробрасывать aria-атрибуты', () => {
      render(<SelectTemplateFilter options={mockOptions} aria-label="test select" aria-required="true" />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('aria-label', 'test select')
      expect(select).toHaveAttribute('aria-required', 'true')
    })
  })

  describe('События', () => {
    it('должен вызывать onFocus при фокусе', () => {
      const handleFocus = jest.fn()
      render(<SelectTemplateFilter options={mockOptions} onFocus={handleFocus} />)
      
      const select = screen.getByTestId('antd-select')
      fireEvent.focus(select)
      
      expect(handleFocus).toHaveBeenCalledTimes(1)
    })

    it('должен вызывать onBlur при потере фокуса', () => {
      const handleBlur = jest.fn()
      render(<SelectTemplateFilter options={mockOptions} onBlur={handleBlur} />)
      
      const select = screen.getByTestId('antd-select')
      fireEvent.blur(select)
      
      expect(handleBlur).toHaveBeenCalledTimes(1)
    })
  })

  describe('Различные режимы работы', () => {
    it('должен работать в режиме multiple', () => {
      render(<SelectTemplateFilter options={mockOptions} mode="multiple" />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-mode', 'multiple')
    })

    it('должен работать в режиме tags', () => {
      render(<SelectTemplateFilter options={mockOptions} mode="tags" />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-mode', 'tags')
    })

    it('должен работать в режиме combobox', () => {
      render(<SelectTemplateFilter options={mockOptions} mode="combobox" />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-mode', 'combobox')
    })
  })

  describe('Обработка пустых и краевых случаев', () => {
    it('должен обрабатывать пустой массив options', () => {
      render(<SelectTemplateFilter options={[]} />)
      
      const nativeSelect = screen.getByTestId('native-select')
      const options = nativeSelect.querySelectorAll('option')
      expect(options).toHaveLength(0)
    })

    it('должен обрабатывать undefined options', () => {
      render(<SelectTemplateFilter options={undefined as any} />)
      
      const nativeSelect = screen.getByTestId('native-select')
      expect(nativeSelect).toBeInTheDocument()
    })

    it('должен обрабатывать null options', () => {
      render(<SelectTemplateFilter options={null as any} />)
      
      const nativeSelect = screen.getByTestId('native-select')
      expect(nativeSelect).toBeInTheDocument()
    })

    it('должен обрабатывать пустые пропсы', () => {
      render(<SelectTemplateFilter options={mockOptions} />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toBeInTheDocument()
    })

    it('должен пробрасывать дополнительные пропсы, которые не указаны в интерфейсе', () => {
      render(<SelectTemplateFilter options={mockOptions} data-custom="custom-value" />)
      
      const select = screen.getByTestId('antd-select')
      expect(select).toHaveAttribute('data-custom', 'custom-value')
    })
  })

  describe('Интеграция с формой', () => {
    it('должен работать внутри формы', () => {
      const handleSubmit = jest.fn((e) => e.preventDefault())
      
      render(
        <form onSubmit={handleSubmit}>
          <SelectTemplateFilter 
            options={mockOptions} 
            name="category" 
            placeholder="Выберите категорию" 
          />
          <button type="submit">Submit</button>
        </form>
      )
      
      const select = screen.getByTestId('antd-select')
      const submitButton = screen.getByText('Submit')
      
      expect(select).toHaveAttribute('data-name', 'category')
      fireEvent.click(submitButton)
      expect(handleSubmit).toHaveBeenCalled()
    })

    it('должен корректно передавать значение в форму', () => {
      const handleChange = jest.fn()
      
      render(
        <form>
          <SelectTemplateFilter 
            options={mockOptions} 
            name="category" 
            onChange={handleChange}
          />
        </form>
      )
      
      const nativeSelect = screen.getByTestId('native-select')
      fireEvent.change(nativeSelect, { target: { value: 'option2' } })
      
      expect(handleChange).toHaveBeenCalled()
    })
  })

  describe('Приоритет пропсов', () => {
    it('переданный className должен объединяться с styles.select, а не заменять его', () => {
      render(<SelectTemplateFilter options={mockOptions} className="custom-class" />)
      
      expect(mockClassName).toHaveBeenCalledWith(
        'mock-select-class',
        'custom-class'
      )
      // Проверяем что styles.select не был потерян
      expect(mockClassName.mock.calls[0][0]).toBe('mock-select-class')
    })
  })
})