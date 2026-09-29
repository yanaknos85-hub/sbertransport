/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import '@testing-library/jest-dom'
import { SelectDepartmentFilter } from '.'

// Мокаем зависимости
jest.mock('classnames', () => ({
  __esModule: true,
  default: jest.fn((...args) => args.filter(Boolean).join(' ')),
}))

jest.mock('./assets/DownArrowIcon', () => {
  return () => <span data-testid="down-arrow-icon">▼</span>
})

jest.mock('./select.module.scss', () => ({
  select: 'mock-select-class',
}))

// Мокаем SelectDepartment - исправляем проблему с value и onChange
jest.mock('modules/OrderExecution/components/SelectDepartment/SelectDepartment', () => ({
  SelectDepartment: jest.fn(({ suffixIcon, className, placeholder, mode, disabled, value, onChange, dropdownClassName, id, style, ...props }) => (
    <div 
      data-testid="select-department" 
      className={className} 
      id={id}
      style={style}
      {...props}
    >
      <div data-testid="suffix-icon">{suffixIcon && suffixIcon({})}</div>
      <div data-testid="placeholder">{placeholder}</div>
      <div data-testid="mode">{mode}</div>
      <div data-testid="disabled">{String(disabled)}</div>
      <div data-testid="value">{value}</div>
      <div data-testid="dropdown-class-name">{dropdownClassName}</div>
      <select 
        data-testid="department-select"
        onChange={onChange}
        defaultValue={value}
      >
        <option value="dept1">Department 1</option>
        <option value="dept2">Department 2</option>
      </select>
    </div>
  )),
}))

describe('SelectDepartmentFilter', () => {
  const mockClassName = jest.requireMock('classnames').default
  const MockSelectDepartment = jest.requireMock('modules/OrderExecution/components/SelectDepartment/SelectDepartment').SelectDepartment

  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('должен рендерить компонент', () => {
    render(<SelectDepartmentFilter />)
    expect(screen.getByTestId('select-department')).toBeInTheDocument()
  })

  it('должен передавать suffixIcon в SelectDepartment', () => {
    render(<SelectDepartmentFilter />)
    expect(MockSelectDepartment).toHaveBeenCalledWith(
      expect.objectContaining({
        suffixIcon: expect.any(Function),
      }),
      expect.anything()
    )
  })

  it('должен применять стили из модуля', () => {
    render(<SelectDepartmentFilter />)
    const select = screen.getByTestId('select-department')
    expect(select).toHaveClass('mock-select-class')
  })

  it('должен объединять переданный className с стилями модуля', () => {
    render(<SelectDepartmentFilter className="custom-class" />)
    expect(mockClassName).toHaveBeenCalledWith('mock-select-class', 'custom-class')
  })

  it('должен пробрасывать placeholder', () => {
    render(<SelectDepartmentFilter placeholder="Выберите отдел" />)
    expect(screen.getByTestId('placeholder')).toHaveTextContent('Выберите отдел')
  })

  it('должен пробрасывать mode', () => {
    render(<SelectDepartmentFilter mode="multiple" />)
    expect(screen.getByTestId('mode')).toHaveTextContent('multiple')
  })

  it('должен пробрасывать value', () => {
    render(<SelectDepartmentFilter value="dept1" />)
    expect(screen.getByTestId('value')).toHaveTextContent('dept1')
  })

  it('должен вызывать onChange при изменении значения', () => {
    const handleChange = jest.fn()
    render(<SelectDepartmentFilter onChange={handleChange} />)
    
    const nativeSelect = screen.getByTestId('department-select')
    fireEvent.change(nativeSelect, { target: { value: 'dept2' } })
    
    expect(handleChange).toHaveBeenCalledTimes(1)
  })

  it('должен пробрасывать dropdownClassName', () => {
    render(<SelectDepartmentFilter dropdownClassName="custom-dropdown" />)
    expect(screen.getByTestId('dropdown-class-name')).toHaveTextContent('custom-dropdown')
  })

  it('должен пробрасывать id', () => {
    render(<SelectDepartmentFilter id="custom-id" />)
    const select = screen.getByTestId('select-department')
    expect(select).toHaveAttribute('id', 'custom-id')
  })

  it('должен пробрасывать style', () => {
    render(<SelectDepartmentFilter style={{ width: '300px' }} />)
    const select = screen.getByTestId('select-department')
    expect(select).toHaveStyle({ width: '300px' })
  })

  it('должен работать без пропсов', () => {
    render(<SelectDepartmentFilter />)
    expect(screen.getByTestId('select-department')).toBeInTheDocument()
    expect(mockClassName).toHaveBeenCalledWith('mock-select-class', undefined)
  })
})