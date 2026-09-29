/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react'
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import SelectOrderDateTime from '.'

const mockSelect = jest.fn()

jest.mock('../../../Inputs/Select', () => {
  const React = require('react')

  return (props: any) => {
    mockSelect(props)

    return (
      <div data-testid="select-order-datetime-mock">
        <div data-testid="select-placeholder">{props.placeholder ?? ''}</div>
        <div data-testid="select-mode">{props.mode ?? ''}</div>
        <div data-testid="select-options">
          {JSON.stringify(props.options)}
        </div>
        <div data-testid="select-allowClear">{String(props.allowClear ?? false)}</div>
        <div data-testid="select-disabled">{String(props.disabled ?? false)}</div>
        <div data-testid="select-value">{props.value ?? ''}</div>
        <div data-testid="select-defaultValue">{props.defaultValue ?? ''}</div>
      </div>
    )
  }
})

describe('SelectOrderDateTime', () => {
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
    render(<SelectOrderDateTime statusHook={statusHookMock} />)

    expect(screen.getByTestId('select-order-datetime-mock')).toBeInTheDocument()
  })

  it('должен передавать mockOptions в Select', () => {
    render(<SelectOrderDateTime statusHook={statusHookMock} />)

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен пробрасывать остальные пропсы в Select', () => {
    render(
      <SelectOrderDateTime
        statusHook={statusHookMock}
        placeholder="Выберите дату и время"
        mode="multiple"
        allowClear
        disabled={false}
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        placeholder: 'Выберите дату и время',
        mode: 'multiple',
        allowClear: true,
        disabled: false,
        options: [{ label: '', value: '' }],
      })
    )

    expect(screen.getByTestId('select-placeholder')).toHaveTextContent('Выберите дату и время')
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

    render(<SelectOrderDateTime statusHook={customStatusHook} />)

    expect(screen.getByTestId('select-order-datetime-mock')).toBeInTheDocument()
  })

  it('должен работать с пустым statusHook', () => {
    const emptyStatusHook = {
      data: [],
      isLoading: false,
      error: null,
    } as any

    render(<SelectOrderDateTime statusHook={emptyStatusHook} />)

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен работать с statusHook в состоянии загрузки', () => {
    const loadingStatusHook = {
      data: null,
      isLoading: true,
      error: null,
    } as any

    render(<SelectOrderDateTime statusHook={loadingStatusHook} />)

    expect(screen.getByTestId('select-order-datetime-mock')).toBeInTheDocument()
    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен работать с statusHook содержащим ошибку', () => {
    const errorStatusHook = {
      data: null,
      isLoading: false,
      error: new Error('Failed to fetch'),
    } as any

    render(<SelectOrderDateTime statusHook={errorStatusHook} />)

    expect(screen.getByTestId('select-order-datetime-mock')).toBeInTheDocument()
    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен пробрасывать className', () => {
    render(
      <SelectOrderDateTime
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
      <SelectOrderDateTime
        statusHook={statusHookMock}
        defaultValue="2024-01-01"
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        defaultValue: '2024-01-01',
        options: [{ label: '', value: '' }],
      })
    )
    expect(screen.getByTestId('select-defaultValue')).toHaveTextContent('2024-01-01')
  })

  it('должен пробрасывать value', () => {
    render(
      <SelectOrderDateTime
        statusHook={statusHookMock}
        value="2024-12-25T10:30:00"
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        value: '2024-12-25T10:30:00',
        options: [{ label: '', value: '' }],
      })
    )
    expect(screen.getByTestId('select-value')).toHaveTextContent('2024-12-25T10:30:00')
  })

  it('должен пробрасывать onChange', () => {
    const mockOnChange = jest.fn()
    
    render(
      <SelectOrderDateTime
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

  it('должен пробрасывать onSearch', () => {
    const mockOnSearch = jest.fn()
    
    render(
      <SelectOrderDateTime
        statusHook={statusHookMock}
        onSearch={mockOnSearch}
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        onSearch: mockOnSearch,
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен пробрасывать onFocus', () => {
    const mockOnFocus = jest.fn()
    
    render(
      <SelectOrderDateTime
        statusHook={statusHookMock}
        onFocus={mockOnFocus}
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        onFocus: mockOnFocus,
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен пробрасывать onBlur', () => {
    const mockOnBlur = jest.fn()
    
    render(
      <SelectOrderDateTime
        statusHook={statusHookMock}
        onBlur={mockOnBlur}
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        onBlur: mockOnBlur,
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен пробрасывать showSearch', () => {
    render(
      <SelectOrderDateTime
        statusHook={statusHookMock}
        showSearch={true}
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        showSearch: true,
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен пробрасывать loading состояние', () => {
    render(
      <SelectOrderDateTime
        statusHook={statusHookMock}
        loading={true}
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        loading: true,
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен корректно обрабатывать mode="tags"', () => {
    render(
      <SelectOrderDateTime
        statusHook={statusHookMock}
        mode="tags"
        placeholder="Введите даты"
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        mode: 'tags',
        placeholder: 'Введите даты',
        options: [{ label: '', value: '' }],
      })
    )
    expect(screen.getByTestId('select-mode')).toHaveTextContent('tags')
  })

  it('должен корректно обрабатывать mode="multiple" с maxTagCount', () => {
    render(
      <SelectOrderDateTime
        statusHook={statusHookMock}
        mode="multiple"
        maxTagCount={3}
        placeholder="Выберите даты"
      />
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        mode: 'multiple',
        maxTagCount: 3,
        placeholder: 'Выберите даты',
        options: [{ label: '', value: '' }],
      })
    )
  })

  it('должен корректно обрабатывать disabled={true}', () => {
    render(
      <SelectOrderDateTime
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
    expect(screen.getByTestId('select-disabled')).toHaveTextContent('true')
  })
})