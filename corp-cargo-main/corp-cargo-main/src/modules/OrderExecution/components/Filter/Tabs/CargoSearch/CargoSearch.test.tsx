/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import moment from 'moment'
import '@testing-library/jest-dom'
import { CargoSearch } from '.'

const mockUseCargoStatuses = jest.fn()
const mockUseAppStoreContext = jest.fn()

jest.mock('mobx-react', () => ({
  observer: (component: React.ComponentType) => component,
}))

jest.mock('api/engineer', () => ({
  useCargoStatuses: () => mockUseCargoStatuses(),
}))

jest.mock('shared/hooks/useAppStoreContext', () => ({
  useAppStoreContext: () => mockUseAppStoreContext(),
}))

jest.mock('utils/searchSymbol', () => ({
  searchSymbol: jest.fn(),
}))

jest.mock('shared/constants/forms.constants', () => ({
  RequestType: {
    SINGLE: 'SINGLE',
    REGULAR: 'REGULAR',
    RELOCATION: 'RELOCATION',
  },
}))

jest.mock('antd/lib/alert/ErrorBoundary', () => {
  return ({ children }: { children: React.ReactNode }) => <>{children}</>
})

jest.mock('../search.module.scss', () => ({
  form: 'form',
  blockTitle: 'blockTitle',
  blockRow: 'blockRow',
  select: 'select',
  formFooter: 'formFooter',
  saveFilter: 'saveFilter',
  actionButtons: 'actionButtons',
  resetFilter: 'resetFilter',
  acceptFilter: 'acceptFilter',
}))

type FormValues = Record<string, any>

const createMockForm = () => {
  const formState: {
    values: FormValues
    onFinish?: (values: FormValues) => void
  } = {
    values: {},
    onFinish: undefined,
  }

  const form = {
    getFieldsValue: jest.fn(() => formState.values),
    setFieldsValue: jest.fn((nextValues: FormValues) => {
      formState.values = {
        ...formState.values,
        ...nextValues,
      }
    }),
    resetFields: jest.fn(() => {
      formState.values = {}
    }),
    submit: jest.fn(() => {
      formState.onFinish?.(formState.values)
    }),
    __state: formState,
  }

  return form
}

let mockForm = createMockForm()

jest.mock('antd/lib/form/Form', () => ({
  useForm: () => [mockForm],
}))

const MockFormContext = React.createContext<any>(null)

jest.mock('antd', () => {
  const React = require('react')

  const Form = ({
    form,
    onFinish,
    children,
    className,
  }: {
    form: any
    onFinish: (values: any) => void
    children: React.ReactNode
    className?: string
  }) => {
    if (form?.__state) {
      form.__state.onFinish = onFinish
    }

    return (
      <MockFormContext.Provider value={form}>
        <form
          data-testid="cargo-search-form"
          className={className}
          onSubmit={(e) => {
            e.preventDefault()
            form?.submit?.()
          }}
        >
          {children}
        </form>
      </MockFormContext.Provider>
    )
  }

  const Button = ({
    children,
    onClick,
    className,
    type,
  }: {
    children: React.ReactNode
    onClick?: () => void
    className?: string
    type?: string
  }) => (
    <button
      type="button"
      className={className}
      data-testid={`button-${children}`}
      data-button-type={type}
      onClick={onClick}
    >
      {children}
    </button>
  )

  return {
    Form,
    Button,
  }
})

jest.mock('../../FormItem', () => {
  const React = require('react')

  return ({
    name,
    label,
    children,
  }: {
    name: string
    label: string
    children: React.ReactElement
  }) => (
    <div data-testid={`form-item-${name}`}>
      <label>{label}</label>
      {React.cloneElement(children, { __fieldName: name })}
    </div>
  )
})

jest.mock('../../Inputs/Input', () => {
  const React = require('react')

  return ({ placeholder, __fieldName }: { placeholder: string; __fieldName?: string }) => {
    const form = React.useContext(MockFormContext)

    const value = __fieldName ? form?.__state?.values?.[__fieldName] ?? '' : ''

    return (
      <input
        data-testid={`input-${__fieldName}`}
        aria-label={placeholder}
        value={value}
        onChange={(e) => {
          if (__fieldName) {
            form.__state.values[__fieldName] = e.target.value
          }
        }}
      />
    )
  }
})

jest.mock('../../Inputs/DatePickerRange', () => {
  const React = require('react')
  const moment = require('moment')

  return {
    DatePickerRange: ({ __fieldName }: { __fieldName?: string }) => {
      const form = React.useContext(MockFormContext)

      return (
        <button
          type="button"
          data-testid={`date-range-${__fieldName}`}
          onClick={() => {
            if (__fieldName) {
              form.__state.values[__fieldName] = {
                value: [moment('2026-01-10T10:00:00.000Z'), moment('2026-01-12T12:30:00.000Z')],
              }
            }
          }}
        >
          set-{__fieldName}
        </button>
      )
    },
  }
})

jest.mock('../../Inputs/SelectDepartmentFilter', () => {
  const React = require('react')

  return {
    SelectDepartmentFilter: ({ __fieldName }: { __fieldName?: string }) => {
      const form = React.useContext(MockFormContext)

      return (
        <button
          type="button"
          data-testid={`department-${__fieldName}`}
          onClick={() => {
            if (__fieldName) {
              form.__state.values[__fieldName] = 'dep-1'
            }
          }}
        >
          department
        </button>
      )
    },
  }
})

jest.mock('../../Inputs/SelectTemplateFilter', () => {
  const React = require('react')

  return {
    SelectTemplateFilter: ({
      __fieldName,
      onChange,
    }: {
      __fieldName?: string
      onChange?: (value: string[]) => void
    }) => {
      const form = React.useContext(MockFormContext)

      return (
        <button
          type="button"
          data-testid={`template-filter-${__fieldName}`}
          onClick={() => {
            const value = ['SINGLE', 'REGULAR']
            if (__fieldName) {
              form.__state.values[__fieldName] = value
            }
            onChange?.(value)
          }}
        >
          request-type
        </button>
      )
    },
  }
})

jest.mock('../components/SelectStatus', () => {
  const React = require('react')

  return ({
    __fieldName,
    onChange,
  }: {
    __fieldName?: string
    onChange?: (value: string[]) => void
  }) => {
    const form = React.useContext(MockFormContext)

    return (
      <button
        type="button"
        data-testid={`status-${__fieldName}`}
        onClick={() => {
          const value = ['NEW', 'DONE']
          if (__fieldName) {
            form.__state.values[__fieldName] = value
          }
          onChange?.(value)
        }}
      >
        status
      </button>
    )
  }
})

describe('CargoSearch', () => {
  let cargoStore: any
  let onClose: jest.Mock

  const renderComponent = (props?: Partial<{ category: string; onClose: () => void }>) => {
    return render(
      <CargoSearch
        category={props?.category ?? 'template'}
        onClose={props?.onClose ?? onClose}
      />,
    )
  }

  beforeEach(() => {
    jest.clearAllMocks()
    mockForm = createMockForm()

    cargoStore = {
      cargoQueryFilters: {},
      activeCategory: 'car',
      setCargoFilterQueryProps: jest.fn(),
      getCargoSchedulerList: jest.fn(),
      getCargoOrderListDeferredPost: jest.fn(),
      resetFilters: jest.fn(),
    }

    onClose = jest.fn()

    mockUseAppStoreContext.mockReturnValue({
      cargoStore,
    })

    mockUseCargoStatuses.mockReturnValue([
      { label: 'Новая', value: 'NEW' },
      { label: 'Завершена', value: 'DONE' },
    ])
  })

  it('рендерит поля для category=template и не показывает поля только для обычной заявки', () => {
    renderComponent({ category: 'template' })

    expect(screen.getByText('Заявка')).toBeInTheDocument()
    expect(screen.getByText('Отправитель')).toBeInTheDocument()
    expect(screen.getByText('Получатель')).toBeInTheDocument()

    expect(screen.getByTestId('form-item-id')).toBeInTheDocument()
    expect(screen.getByTestId('form-item-statuses')).toBeInTheDocument()
    expect(screen.getByTestId('form-item-creationTime')).toBeInTheDocument()

    expect(screen.queryByTestId('form-item-controlTime')).not.toBeInTheDocument()
    expect(screen.queryByTestId('form-item-desiredTime')).not.toBeInTheDocument()
    expect(screen.queryByTestId('form-item-requestType')).not.toBeInTheDocument()
  })

  it('рендерит дополнительные поля для category!=template', () => {
    renderComponent({ category: 'order' })

    expect(screen.getByTestId('form-item-controlTime')).toBeInTheDocument()
    expect(screen.getByTestId('form-item-desiredTime')).toBeInTheDocument()
    expect(screen.getByTestId('form-item-requestType')).toBeInTheDocument()
  })

  it('вызывает setCargoFilterQueryProps при изменении status', () => {
    renderComponent({ category: 'template' })

    fireEvent.click(screen.getByTestId('status-statuses'))

    expect(cargoStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
      statuses: ['NEW', 'DONE'],
    })
  })

  it('вызывает setCargoFilterQueryProps при изменении requestType', () => {
    renderComponent({ category: 'order' })

    fireEvent.click(screen.getByTestId('template-filter-requestType'))

    expect(cargoStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
      requestType: ['SINGLE', 'REGULAR'],
    })
  })

  it('сохраняет фильтр через saveFilters', () => {
    renderComponent({ category: 'template' })

    mockForm.__state.values = {
      id: '12345',
      senderName: 'Иван',
    }

    fireEvent.click(screen.getByText('Сохранить фильтр'))

    expect(cargoStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
      id: '12345',
      senderName: 'Иван',
    })
  })

  it('сбрасывает фильтры через handleReset', () => {
    renderComponent({ category: 'template' })

    fireEvent.click(screen.getByText('Сбросить'))

    expect(cargoStore.resetFilters).toHaveBeenCalledTimes(1)
    expect(mockForm.resetFields).toHaveBeenCalledTimes(1)
  })

  it('для category=template сабмитит форму, преобразует даты и вызывает getCargoSchedulerList', () => {
    renderComponent({ category: 'template' })

    mockForm.__state.values = {
      id: '777',
      senderName: 'Петров',
      creationTime: {
        value: [
          moment('2026-02-01T10:00:00.000Z'),
          moment('2026-02-03T15:45:00.000Z'),
        ],
      },
    }

    fireEvent.click(screen.getByText('Подтвердить'))

    expect(cargoStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
      id: '777',
      senderName: 'Петров',
      transportType: 'CAR',
      creationTimeFrom: '2026-02-01T10:00:00.000Z',
      creationTimeTo: '2026-02-03T15:45:00.000Z',
      controlTimeFrom: undefined,
      controlTimeTo: undefined,
      desiredTimeFrom: undefined,
      desiredTimeTo: undefined,
    })

    expect(cargoStore.getCargoSchedulerList).toHaveBeenCalledTimes(1)
    expect(cargoStore.getCargoOrderListDeferredPost).not.toHaveBeenCalled()
    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('для category!=template сабмитит форму и вызывает getCargoOrderListDeferredPost', () => {
    renderComponent({ category: 'order' })

    mockForm.__state.values = {
      id: '999',
      controlTime: {
        value: [
          moment('2026-03-01T08:00:00.000Z'),
          moment('2026-03-02T18:00:00.000Z'),
        ],
      },
      desiredTime: {
        value: [
          moment('2026-03-05T09:30:00.000Z'),
          moment('2026-03-06T19:00:00.000Z'),
        ],
      },
    }

    fireEvent.click(screen.getByText('Подтвердить'))

    expect(cargoStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
      id: '999',
      transportType: 'CAR',
      creationTimeFrom: undefined,
      creationTimeTo: undefined,
      controlTimeFrom: '2026-03-01T08:00:00.000Z',
      controlTimeTo: '2026-03-02T18:00:00.000Z',
      desiredTimeFrom: '2026-03-05T09:30:00.000Z',
      desiredTimeTo: '2026-03-06T19:00:00.000Z',
    })

    expect(cargoStore.getCargoOrderListDeferredPost).toHaveBeenCalledTimes(1)
    expect(cargoStore.getCargoSchedulerList).not.toHaveBeenCalled()
    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('если activeCategory=all, transportType должен быть undefined', () => {
    cargoStore.activeCategory = 'all'

    renderComponent({ category: 'order' })

    mockForm.__state.values = {
      id: '111',
    }

    fireEvent.click(screen.getByText('Подтвердить'))

    expect(cargoStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
      id: '111',
      transportType: undefined,
      creationTimeFrom: undefined,
      creationTimeTo: undefined,
      controlTimeFrom: undefined,
      controlTimeTo: undefined,
      desiredTimeFrom: undefined,
      desiredTimeTo: undefined,
    })
  })

  it('при невалидном creationTime проставляет creationTimeFrom/To как undefined', () => {
    renderComponent({ category: 'template' })

    mockForm.__state.values = {
      creationTime: {
        value: [moment('2026-02-01T10:00:00.000Z')],
      },
    }

    fireEvent.click(screen.getByText('Подтвердить'))

    expect(cargoStore.setCargoFilterQueryProps).toHaveBeenCalledWith({
      transportType: 'CAR',
      creationTimeFrom: undefined,
      creationTimeTo: undefined,
      controlTimeFrom: undefined,
      controlTimeTo: undefined,
      desiredTimeFrom: undefined,
      desiredTimeTo: undefined,
    })
  })

  it('инициализирует форму значениями из cargoStore.cargoQueryFilters', () => {
    cargoStore.cargoQueryFilters = {
      id: 'ABC-123',
      senderName: 'Тестовый пользователь',
      creationTimeFrom: '2026-01-01T00:00:00.000Z',
      creationTimeTo: '2026-01-02T00:00:00.000Z',
      controlTimeFrom: '2026-01-03T00:00:00.000Z',
      controlTimeTo: '2026-01-04T00:00:00.000Z',
      desiredTimeFrom: '2026-01-05T00:00:00.000Z',
      desiredTimeTo: '2026-01-06T00:00:00.000Z',
    }

    renderComponent({ category: 'order' })

    expect(mockForm.setFieldsValue).toHaveBeenCalledTimes(1)

    const arg = mockForm.setFieldsValue.mock.calls[0][0]

    expect(arg.id).toBe('ABC-123')
    expect(arg.senderName).toBe('Тестовый пользователь')

    expect(arg.creationTime.value).toHaveLength(2)
    expect(moment.isMoment(arg.creationTime.value[0])).toBe(true)
    expect(moment.isMoment(arg.creationTime.value[1])).toBe(true)

    expect(arg.controlTime.value).toHaveLength(2)
    expect(moment.isMoment(arg.controlTime.value[0])).toBe(true)
    expect(moment.isMoment(arg.controlTime.value[1])).toBe(true)

    expect(arg.desiredTime.value).toHaveLength(2)
    expect(moment.isMoment(arg.desiredTime.value[0])).toBe(true)
    expect(moment.isMoment(arg.desiredTime.value[1])).toBe(true)
  })

  it('сбрасывает форму при смене category', () => {
    const { rerender } = render(
      <CargoSearch category="template" onClose={onClose} />,
    )

    expect(mockForm.resetFields).not.toHaveBeenCalled()

    rerender(<CargoSearch category="order" onClose={onClose} />)

    expect(mockForm.resetFields).toHaveBeenCalledTimes(1)
  })
})