/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react'
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import SelectCorpClient from '.'

const mockUseOrganizations = jest.fn()
const mockSelect = jest.fn()

jest.mock('api/organizations', () => ({
  useOrganizations: () => mockUseOrganizations(),
}))

jest.mock('../../../Inputs/Select', () => {
  const React = require('react')

  return (props: any) => {
    mockSelect(props)

    return (
      <div data-testid="select-corp-client-mock">
        <div data-testid="select-placeholder">{props.placeholder ?? ''}</div>
        <div data-testid="select-mode">{props.mode ?? ''}</div>
        <div data-testid="select-options">
          {JSON.stringify(props.options)}
        </div>
      </div>
    )
  }
})

describe('SelectCorpClient', () => {
  const statusHookMock = {} as any

  beforeEach(() => {
    jest.clearAllMocks()

    mockUseOrganizations.mockReturnValue({
      data: {
        organizationResponse: {
          content: [
            {
              id: 'org-1',
              officialName: 'ООО Ромашка',
            },
            {
              id: 'org-2',
              officialName: 'АО Альфа',
            },
            {
              id: 'org-3',
              officialName: 'ИП Тест',
            },
          ],
        },
      },
    })
  })

  it('должен рендериться', () => {
    render(<SelectCorpClient statusHook={statusHookMock} />)

    expect(screen.getByTestId('select-corp-client-mock')).toBeInTheDocument()
  })

  it('должен преобразовывать organizations в options', () => {
    render(<SelectCorpClient statusHook={statusHookMock} />)

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        options: [
          { label: 'ООО Ромашка', value: 'org-1' },
          { label: 'АО Альфа', value: 'org-2' },
          { label: 'ИП Тест', value: 'org-3' },
        ],
      })
    )
  })

  it('должен пробрасывать остальные пропсы в Select', () => {
    render(
      <SelectCorpClient
        statusHook={statusHookMock}
        placeholder="Выберите организацию"
        mode="multiple"
        allowClear
      />,
    )

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        placeholder: 'Выберите организацию',
        mode: 'multiple',
        allowClear: true,
        options: [
          { label: 'ООО Ромашка', value: 'org-1' },
          { label: 'АО Альфа', value: 'org-2' },
          { label: 'ИП Тест', value: 'org-3' },
        ],
      })
    )

    expect(screen.getByTestId('select-placeholder')).toHaveTextContent('Выберите организацию')
    expect(screen.getByTestId('select-mode')).toHaveTextContent('multiple')
  })

  it('должен вызывать useOrganizations', () => {
    render(<SelectCorpClient statusHook={statusHookMock} />)

    expect(mockUseOrganizations).toHaveBeenCalledTimes(1)
  })

  it('должен работать с пустым списком организаций', () => {
    mockUseOrganizations.mockReturnValue({
      data: {
        organizationResponse: {
          content: [],
        },
      },
    })

    render(<SelectCorpClient statusHook={statusHookMock} />)

    expect(mockSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        options: [],
      })
    )
  })

  it('должен принимать statusHook и не падать, даже если он не используется внутри', () => {
    const customStatusHook = {
      data: [{ id: 1, name: 'test' }],
      isLoading: false,
      error: null,
    } as any

    render(<SelectCorpClient statusHook={customStatusHook} />)

    expect(screen.getByTestId('select-corp-client-mock')).toBeInTheDocument()
  })
})