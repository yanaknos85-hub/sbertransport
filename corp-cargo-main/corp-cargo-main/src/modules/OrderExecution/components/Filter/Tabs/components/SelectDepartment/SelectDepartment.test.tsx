/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react'
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import SelectDepartment from '.'

const mockSelect = jest.fn()

jest.mock('../../../Inputs/Select', () => {
  const React = require('react')

  return (props: any) => {
    mockSelect(props)

    return (
      <div data-testid="select-department-mock">
        <div data-testid="select-placeholder">{props.placeholder ?? ''}</div>
        <div data-testid="select-mode">{props.mode ?? ''}</div>
        <div data-testid="select-options">
          {JSON.stringify(props.options)}
        </div>
        <div data-testid="select-allowClear">{String(props.allowClear ?? false)}</div>
        <div data-testid="select-disabled">{String(props.disabled ?? false)}</div>
      </div>
    )
  }
})

describe('SelectDepartment', () => {
  const statusHookMock = {
    data: [
      { id: 1, name: 'Status 1' },
      { id: 2, name: 'Status 2' },
    ],
    isLoading: false,
    error: null,
  } as any

  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('должен рендериться', () => {
    render(<SelectDepartment statusHook={statusHookMock} />)

    expect(screen.getByTestId('select-department-mock')).toBeInTheDocument()
  })

  it('должен передавать mockOptions в Select', () => {
    render(<SelectDepartment statusHook={statusHookMock} />)

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен пробрасывать остальные пропсы в Select', () => {
    render(
      <SelectDepartment
        statusHook={statusHookMock}
        placeholder="Выберите подразделение"
        mode="multiple"
        allowClear
        disabled={false}
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        placeholder: 'Выберите подразделение',
        mode: 'multiple',
        allowClear: true,
        disabled: false,
        options: [{ label: '', value: '' }],
      })
    )

    expect(screen.getByTestId('select-placeholder')).toHaveTextContent('Выберите подразделение')
    expect(screen.getByTestId('select-mode')).toHaveTextContent('multiple')
    expect(screen.getByTestId('select-allowClear')).toHaveTextContent('true')
    expect(screen.getByTestId('select-disabled')).toHaveTextContent('false')
  })

  it('должен принимать statusHook и не падать, даже если он не используется внутри', () => {
    const customStatusHook = {
      data: [{ id: 1, name: 'test' }],
      isLoading: true,
      error: null,
    } as any

    render(<SelectDepartment statusHook={customStatusHook} />)

    expect(screen.getByTestId('select-department-mock')).toBeInTheDocument()
  })

  it('должен работать с пустым statusHook', () => {
    const emptyStatusHook = {
      data: [],
      isLoading: false,
      error: null,
    } as any

    render(<SelectDepartment statusHook={emptyStatusHook} />)

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен пробрасывать className', () => {
    render(
      <SelectDepartment
        statusHook={statusHookMock}
        className="custom-select-class"
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        className: 'custom-select-class',
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен пробрасывать defaultValue', () => {
    render(
      <SelectDepartment
        statusHook={statusHookMock}
        defaultValue="test-value"
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        defaultValue: 'test-value',
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен пробрасывать onChange', () => {
    const mockOnChange = jest.fn()
    
    render(
      <SelectDepartment
        statusHook={statusHookMock}
        onChange={mockOnChange}
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        onChange: mockOnChange,
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен пробрасывать value', () => {
    render(
      <SelectDepartment
        statusHook={statusHookMock}
        value="selected-value"
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        value: 'selected-value',
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен корректно обрабатывать mode="tags"', () => {
    render(
      <SelectDepartment
        statusHook={statusHookMock}
        mode="tags"
        placeholder="Введите теги"
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        mode: 'tags',
        placeholder: 'Введите теги',
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен корректно обрабатывать disabled={true}', () => {
    render(
      <SelectDepartment
        statusHook={statusHookMock}
        disabled={true}
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        disabled: true,
        options: [{ label: '', value: '' }],
      })
    )
  })
})