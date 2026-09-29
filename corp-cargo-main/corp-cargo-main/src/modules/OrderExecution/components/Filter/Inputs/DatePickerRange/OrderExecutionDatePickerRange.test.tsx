/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import '@testing-library/jest-dom'
import moment from 'moment'
import { DatePickerRange } from '.'

const mockRangePicker = jest.fn()
const mockGetFormatDateInput = jest.fn()
const mockGetFormatRangeInput = jest.fn()

jest.mock('./dateInput.module.scss', () => ({
  datePicker: 'datePicker',
}))

jest.mock('styled-components', () => {
  const React = require('react')

  return {
    __esModule: true,
    default: {
      div:
        () =>
        ({ children }: { children: React.ReactNode }) =>
          <div data-testid="styled-date-input">{children}</div>,
    },
  }
})

jest.mock('@ant-design/icons', () => ({
  MinusOutlined: () => <span data-testid="minus-icon">minus</span>,
}))

jest.mock('shared/icons/calendar.svg', () => ({
  ReactComponent: () => <span data-testid="calendar-icon">calendar</span>,
}))

jest.mock('./utils', () => ({
  getFormatDateInput: (...args: any[]) => mockGetFormatDateInput(...args),
  getFormatRangeInput: (...args: any[]) => mockGetFormatRangeInput(...args),
}))

jest.mock('antd', () => {
  const React = require('react')

  const RangePicker = (props: any) => {
    mockRangePicker(props)

    return (
      <div data-testid="range-picker">
        <div data-testid="range-picker-format">{String(props.format)}</div>
        <div data-testid="range-picker-show-time">{String(!!props.showTime)}</div>
        <div data-testid="range-picker-classname">{props.className ?? ''}</div>
        <div data-testid="range-picker-placeholder">{JSON.stringify(props.placeholder)}</div>
        <div data-testid="range-picker-value">
          {JSON.stringify(
            (props.value || []).map((item: any) => (moment.isMoment(item) ? item.toISOString() : item)),
          )}
        </div>

        <button
          type="button"
          data-testid="trigger-onchange"
          onClick={() =>
            props.onChange?.([
              moment('2026-04-01T10:00:00.000Z'),
              moment('2026-04-03T15:30:00.000Z'),
            ])
          }
        >
          trigger-onchange
        </button>

        <button
          type="button"
          data-testid="trigger-onchange-null"
          onClick={() => props.onChange?.(null)}
        >
          trigger-onchange-null
        </button>

        <button
          type="button"
          data-testid="check-disabled-future-date"
          onClick={() => {
            const result = props.disabledDate?.(moment('2099-01-01T12:00:00.000Z'))
            ;(global as any).__disabledDateResult = result
          }}
        >
          check-disabled-future-date
        </button>

        <button
          type="button"
          data-testid="check-disabled-past-date"
          onClick={() => {
            const result = props.disabledDate?.(moment('2000-01-01T12:00:00.000Z'))
            ;(global as any).__disabledDatePastResult = result
          }}
        >
          check-disabled-past-date
        </button>

        <button
          type="button"
          data-testid="check-disabled-time"
          onClick={() => {
            const result = props.disabledTime?.(moment())
            ;(global as any).__disabledTimeResult = result
          }}
        >
          check-disabled-time
        </button>

        <div data-testid="separator">{props.separator}</div>
        <div data-testid="suffix-icon">{props.suffixIcon}</div>
      </div>
    )
  }

  return {
    DatePicker: {
      RangePicker,
    },
  }
})

describe('DatePickerRange', () => {
  beforeEach(() => {
    jest.clearAllMocks()

    mockGetFormatDateInput.mockReturnValue({
      format: 'DD.MM.YYYY',
    })

    mockGetFormatRangeInput.mockReturnValue(['Дата начала', 'Дата окончания'])

    ;(global as any).__disabledDateResult = undefined
    ;(global as any).__disabledDatePastResult = undefined
    ;(global as any).__disabledTimeResult = undefined
  })

  it('должен рендериться', () => {
    render(<DatePickerRange />)

    expect(screen.getByTestId('styled-date-input')).toBeInTheDocument()
    expect(screen.getByTestId('range-picker')).toBeInTheDocument()
  })

  it('должен передавать значения по умолчанию', () => {
    render(<DatePickerRange />)

    const props = mockRangePicker.mock.calls[0][0]

    expect(props.value).toEqual([null, null])
    expect(props.showTime).toBe(false)
    expect(props.disabledTime).toBeUndefined()
    expect(props.className).toBe('datePicker')
    expect(props.allowEmpty).toEqual([true, true])
  })

  it('должен вызывать getFormatDateInput с правильными аргументами', () => {
    const start = moment('2026-03-10T09:00:00.000Z')

    render(
      <DatePickerRange
        value={{ mode: 'range', value: [start, null] }}
        dateTimeShow
        rangeTimeShow={false}
      />,
    )

    expect(mockGetFormatDateInput).toHaveBeenCalledWith('range', true, false, start)
  })

  it('должен вызывать getFormatRangeInput с правильными аргументами', () => {
    const start = moment('2026-03-10T09:00:00.000Z')
    const end = moment('2026-03-11T11:00:00.000Z')

    render(
      <DatePickerRange
        value={{ mode: 'range', value: [start, end] }}
      />,
    )

    expect(mockGetFormatRangeInput).toHaveBeenCalledWith('range', start, end)
  })

  it('должен передавать format и placeholder в RangePicker', () => {
    render(<DatePickerRange />)

    expect(screen.getByTestId('range-picker-format')).toHaveTextContent('DD.MM.YYYY')
    expect(screen.getByTestId('range-picker-placeholder')).toHaveTextContent(
      JSON.stringify(['Дата начала', 'Дата окончания']),
    )
  })

  it('должен передавать value в RangePicker', () => {
    const start = moment('2026-04-10T08:00:00.000Z')
    const end = moment('2026-04-11T18:30:00.000Z')

    render(
      <DatePickerRange
        value={{ mode: 'range', value: [start, end] }}
      />,
    )

    expect(screen.getByTestId('range-picker-value')).toHaveTextContent(
      JSON.stringify([start.toISOString(), end.toISOString()]),
    )
  })

  it('должен вызывать onChange с выбранными датами', () => {
    const onChange = jest.fn()

    render(<DatePickerRange onChange={onChange} />)

    fireEvent.click(screen.getByTestId('trigger-onchange'))

    expect(onChange).toHaveBeenCalledWith({
      mode: 'range',
      value: [
        moment('2026-04-01T10:00:00.000Z'),
        moment('2026-04-03T15:30:00.000Z'),
      ],
    })
  })

  it('должен вызывать onChange с [null, null], если RangePicker вернул null', () => {
    const onChange = jest.fn()

    render(<DatePickerRange onChange={onChange} />)

    fireEvent.click(screen.getByTestId('trigger-onchange-null'))

    expect(onChange).toHaveBeenCalledWith({
      mode: 'range',
      value: [null, null],
    })
  })

  it('должен включать showTime когда rangeTimeShow=true', () => {
    render(<DatePickerRange rangeTimeShow />)

    expect(screen.getByTestId('range-picker-show-time')).toHaveTextContent('true')

    const props = mockRangePicker.mock.calls[0][0]
    expect(typeof props.disabledTime).toBe('function')
  })

  it('не должен передавать disabledTime когда rangeTimeShow=false', () => {
    render(<DatePickerRange rangeTimeShow={false} />)

    const props = mockRangePicker.mock.calls[0][0]
    expect(props.disabledTime).toBeUndefined()
  })

  it('должен запрещать будущую дату если isDisabledFutureDate=true', () => {
    render(<DatePickerRange isDisabledFutureDate />)

    fireEvent.click(screen.getByTestId('check-disabled-future-date'))
    fireEvent.click(screen.getByTestId('check-disabled-past-date'))

    expect((global as any).__disabledDateResult).toBe(true)
    expect((global as any).__disabledDatePastResult).toBe(false)
  })

  it('не должен запрещать даты если isDisabledFutureDate=false', () => {
    render(<DatePickerRange isDisabledFutureDate={false} />)

    fireEvent.click(screen.getByTestId('check-disabled-future-date'))

    expect((global as any).__disabledDateResult).toBe(false)
  })

  it('должен возвращать disabledHours и disabledMinutes если rangeTimeShow=true', () => {
    render(<DatePickerRange rangeTimeShow isDisabledFutureDate />)

    fireEvent.click(screen.getByTestId('check-disabled-time'))

    const result = (global as any).__disabledTimeResult

    expect(result).toBeDefined()
    expect(typeof result.disabledHours).toBe('function')
    expect(typeof result.disabledMinutes).toBe('function')

    const disabledHours = result.disabledHours()
    const disabledMinutes = result.disabledMinutes()

    expect(Array.isArray(disabledHours)).toBe(true)
    expect(Array.isArray(disabledMinutes)).toBe(true)
  })

  it('должен передавать allowEmpty=[true, true] по умолчанию', () => {
    render(<DatePickerRange />)

    const props = mockRangePicker.mock.calls[0][0]
    expect(props.allowEmpty).toEqual([true, true])
  })

  it('должен передавать allowEmpty=false если allowEmpty={false}', () => {
    render(<DatePickerRange allowEmpty={false} />)

    const props = mockRangePicker.mock.calls[0][0]
    expect(props.allowEmpty).toBe(false)
  })

  it('должен рендерить separator и suffixIcon', () => {
    render(<DatePickerRange />)

    expect(screen.getByTestId('minus-icon')).toBeInTheDocument()
    expect(screen.getByTestId('calendar-icon')).toBeInTheDocument()
  })

  it('должен передавать inputReadOnly=true', () => {
    render(<DatePickerRange />)

    const props = mockRangePicker.mock.calls[0][0]
    expect(props.inputReadOnly).toBe(true)
  })

  it('должен передавать getPopupContainer', () => {
    render(<DatePickerRange />)

    const props = mockRangePicker.mock.calls[0][0]
    expect(typeof props.getPopupContainer).toBe('function')
  })
})