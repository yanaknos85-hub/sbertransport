/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react'
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import SelectStatus from '.'

const mockSelect = jest.fn()

jest.mock('../../../Inputs/Select', () => {
  const React = require('react')

  return (props: any) => {
    mockSelect(props)

    return (
      <div data-testid="select-status-mock">
        <div data-testid="select-placeholder">{props.placeholder ?? ''}</div>
        <div data-testid="select-mode">{props.mode ?? ''}</div>
        <div data-testid="select-options">
          {JSON.stringify(props.options)}
        </div>
        <div data-testid="select-allowClear">{String(props.allowClear ?? false)}</div>
        <div data-testid="select-disabled">{String(props.disabled ?? false)}</div>
        <div data-testid="select-getPopupContainer">
          {props.getPopupContainer ? 'has getPopupContainer' : 'no getPopupContainer'}
        </div>
      </div>
    )
  }
})

jest.mock('modules/ServiceMetrics/TransportStatuses', () => ({
  TransportStatuses: {
    cargo: ['NEW', 'IN_PROGRESS', 'DONE', 'CANCELLED'],
  },
}))

describe('SelectStatus', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('Базовый функционал', () => {
    it('должен рендериться', () => {
      const statusHookMock = {
        data: [
          { name: 'NEW', rusName: 'Новая' },
          { name: 'IN_PROGRESS', rusName: 'В процессе' },
        ],
        isLoading: false,
        error: null,
      } as any

      render(<SelectStatus statusHook={statusHookMock} />)

      expect(screen.getByTestId('select-status-mock')).toBeInTheDocument()
    })

    it('должен правильно преобразовывать статусы в options', () => {
      const statusHookMock = {
        data: [
          { name: 'NEW', rusName: 'Новая' },
          { name: 'IN_PROGRESS', rusName: 'В процессе' },
          { name: 'DONE', rusName: 'Завершена' },
          { name: 'CANCELLED', rusName: 'Отменена' },
        ],
        isLoading: false,
        error: null,
      } as any

      render(<SelectStatus statusHook={statusHookMock} />)

      expect(mockSelect).toHaveBeenCalledWith(
        expect.objectContaining({
          options: [
            { label: 'Новая', value: 'NEW' },
            { label: 'В процессе', value: 'IN_PROGRESS' },
            { label: 'Завершена', value: 'DONE' },
            { label: 'Отменена', value: 'CANCELLED' },
          ],
        })
      )
    })

    it('должен фильтровать статусы только из TransportStatuses.cargo', () => {
      const statusHookMock = {
        data: [
          { name: 'NEW', rusName: 'Новая' },
          { name: 'IN_PROGRESS', rusName: 'В процессе' },
          { name: 'DONE', rusName: 'Завершена' },
          { name: 'CANCELLED', rusName: 'Отменена' },
          { name: 'UNKNOWN', rusName: 'Неизвестный статус' },
          { name: 'PENDING', rusName: 'Ожидание' },
        ],
        isLoading: false,
        error: null,
      } as any

      render(<SelectStatus statusHook={statusHookMock} />)

      expect(mockSelect).toHaveBeenCalledWith(
        expect.objectContaining({
          options: [
            { label: 'Новая', value: 'NEW' },
            { label: 'В процессе', value: 'IN_PROGRESS' },
            { label: 'Завершена', value: 'DONE' },
            { label: 'Отменена', value: 'CANCELLED' },
          ],
        })
      )
    })
  })

  describe('Проброс пропсов', () => {
    it('должен пробрасывать остальные пропсы в Select', () => {
      const statusHookMock = {
        data: [
          { name: 'NEW', rusName: 'Новая' },
          { name: 'DONE', rusName: 'Завершена' },
        ],
        isLoading: false,
        error: null,
      } as any

      render(
        <SelectStatus
          statusHook={statusHookMock}
          placeholder="Выберите статус"
          mode="multiple"
          allowClear
          disabled={false}
        />
      )

      expect(mockSelect).toHaveBeenCalledWith(
        expect.objectContaining({
          placeholder: 'Выберите статус',
          mode: 'multiple',
          allowClear: true,
          disabled: false,
          options: [
            { label: 'Новая', value: 'NEW' },
            { label: 'Завершена', value: 'DONE' },
          ],
        })
      )
    })

    it('должен пробрасывать className', () => {
      const statusHookMock = {
        data: [
          { name: 'NEW', rusName: 'Новая' },
        ],
        isLoading: false,
        error: null,
      } as any

      render(
        <SelectStatus
          statusHook={statusHookMock}
          className="custom-select-class"
        />
      )

      expect(mockSelect).toHaveBeenCalledWith(
        expect.objectContaining({
          className: 'custom-select-class',
        })
      )
    })

    it('должен пробрасывать onChange', () => {
      const mockOnChange = jest.fn()
      const statusHookMock = {
        data: [
          { name: 'NEW', rusName: 'Новая' },
        ],
        isLoading: false,
        error: null,
      } as any

      render(
        <SelectStatus
          statusHook={statusHookMock}
          onChange={mockOnChange}
        />
      )

      expect(mockSelect).toHaveBeenCalledWith(
        expect.objectContaining({
          onChange: mockOnChange,
        })
      )
    })

    it('должен пробрасывать defaultValue', () => {
      const statusHookMock = {
        data: [
          { name: 'NEW', rusName: 'Новая' },
        ],
        isLoading: false,
        error: null,
      } as any

      render(
        <SelectStatus
          statusHook={statusHookMock}
          defaultValue="NEW"
        />
      )

      expect(mockSelect).toHaveBeenCalledWith(
        expect.objectContaining({
          defaultValue: 'NEW',
        })
      )
    })
  })

  describe('Краевые случаи', () => {
    it('должен работать с пустым массивом данных', () => {
      const statusHookMock = {
        data: [],
        isLoading: false,
        error: null,
      } as any

      render(<SelectStatus statusHook={statusHookMock} />)

      expect(mockSelect).toHaveBeenCalledWith(
        expect.objectContaining({
          options: [],
        })
      )
    })

    it('должен возвращать пустой массив если все статусы отфильтрованы', () => {
      const statusHookMock = {
        data: [
          { name: 'UNKNOWN', rusName: 'Неизвестный' },
          { name: 'PENDING', rusName: 'Ожидание' },
          { name: 'PROCESSING', rusName: 'Обработка' },
        ],
        isLoading: false,
        error: null,
      } as any

      render(<SelectStatus statusHook={statusHookMock} />)

      expect(mockSelect).toHaveBeenCalledWith(
        expect.objectContaining({
          options: [],
        })
      )
    })

    it('должен корректно обрабатывать статусы с пустым rusName', () => {
      const statusHookMock = {
        data: [
          { name: 'NEW', rusName: '' },
          { name: 'IN_PROGRESS', rusName: 'В процессе' },
        ],
        isLoading: false,
        error: null,
      } as any

      render(<SelectStatus statusHook={statusHookMock} />)

      expect(mockSelect).toHaveBeenCalledWith(
        expect.objectContaining({
          options: [
            { label: '', value: 'NEW' },
            { label: 'В процессе', value: 'IN_PROGRESS' },
          ],
        })
      )
    })
  })

  describe('getPopupContainer', () => {
    it('должен передавать getPopupContainer функцию', () => {
      const statusHookMock = {
        data: [
          { name: 'NEW', rusName: 'Новая' },
        ],
        isLoading: false,
        error: null,
      } as any

      render(<SelectStatus statusHook={statusHookMock} />)

      expect(mockSelect).toHaveBeenCalledWith(
        expect.objectContaining({
          getPopupContainer: expect.any(Function),
        })
      )
    })

    it('getPopupContainer должен возвращать parentNode триггера', () => {
      const statusHookMock = {
        data: [
          { name: 'NEW', rusName: 'Новая' },
        ],
        isLoading: false,
        error: null,
      } as any

      render(<SelectStatus statusHook={statusHookMock} />)

      const callArgs = mockSelect.mock.calls[0][0]
      const mockTrigger = { parentNode: document.body }
      
      const result = callArgs.getPopupContainer(mockTrigger)
      
      expect(result).toBe(document.body)
    })
  })

  describe('Различные режимы Select', () => {
    it('должен поддерживать mode="multiple"', () => {
      const statusHookMock = {
        data: [
          { name: 'NEW', rusName: 'Новая' },
          { name: 'DONE', rusName: 'Завершена' },
        ],
        isLoading: false,
        error: null,
      } as any

      render(
        <SelectStatus
          statusHook={statusHookMock}
          mode="multiple"
        />
      )

      expect(mockSelect).toHaveBeenCalledWith(
        expect.objectContaining({
          mode: 'multiple',
          options: [
            { label: 'Новая', value: 'NEW' },
            { label: 'Завершена', value: 'DONE' },
          ],
        })
      )
      expect(screen.getByTestId('select-mode')).toHaveTextContent('multiple')
    })

    it('должен поддерживать showSearch', () => {
      const statusHookMock = {
        data: [
          { name: 'NEW', rusName: 'Новая' },
        ],
        isLoading: false,
        error: null,
      } as any

      render(
        <SelectStatus
          statusHook={statusHookMock}
          showSearch={true}
        />
      )

      expect(mockSelect).toHaveBeenCalledWith(
        expect.objectContaining({
          showSearch: true,
        })
      )
    })

    it('должен поддерживать loading состояние', () => {
      const statusHookMock = {
        data: [
          { name: 'NEW', rusName: 'Новая' },
        ],
        isLoading: false,
        error: null,
      } as any

      render(
        <SelectStatus
          statusHook={statusHookMock}
          loading={true}
        />
      )

      expect(mockSelect).toHaveBeenCalledWith(
        expect.objectContaining({
          loading: true,
        })
      )
    })
  })
})